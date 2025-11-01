package com.roulettedraft.service;

import com.roulettedraft.domain.model.DraftPick;
import com.roulettedraft.domain.model.Room;
import com.roulettedraft.domain.model.RoomParticipant;
import com.roulettedraft.domain.model.RoomStatus;
import com.roulettedraft.dto.RoomDto;
import com.roulettedraft.dto.websocket.ErrorEvent;
import com.roulettedraft.dto.websocket.PickMadeEvent;
import com.roulettedraft.mapper.DtoMapper;
import com.roulettedraft.repository.DraftPickRepository;
import com.roulettedraft.repository.PlayerRepository;
import com.roulettedraft.repository.RoomParticipantRepository;
import com.roulettedraft.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DraftService {
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final DraftPickRepository draftPickRepository;
    private final PlayerRepository playerRepository;
    private final WebSocketService webSocketService;
    private final MongoTemplate mongoTemplate;
    private final TimerService timerService;
    private final DtoMapper dtoMapper;

    @Transactional
    public void makePick(String roomId, String userId, String playerId) {
        log.info("[room:{}] User {} picking player {}", roomId, userId, playerId);
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    sendError(roomId, "ROOM_NOT_FOUND", "Room not found");
                    return new RuntimeException("Room not found: " + roomId);
                });
        
        // Validate room status
        if (room.getStatus() != RoomStatus.DRAFTING) {
            sendError(roomId, "INVALID_STATUS", "Room is not in DRAFTING status");
            throw new RuntimeException("Room is not in DRAFTING status");
        }
        
        // Validate turn
        List<String> pickOrder = room.getPickOrder();
        if (room.getCurrentPickIndex() >= pickOrder.size()) {
            sendError(roomId, "DRAFT_COMPLETE", "Draft is complete");
            throw new RuntimeException("Draft is complete");
        }
        
        String currentUserId = pickOrder.get(room.getCurrentPickIndex());
        if (!currentUserId.equals(userId)) {
            sendError(roomId, "NOT_YOUR_TURN", "It's not your turn");
            throw new RuntimeException("Not your turn. Current player: " + currentUserId);
        }
        
        // Check if player already picked
        if (draftPickRepository.existsByRoomIdAndPlayerId(roomId, playerId)) {
            sendError(roomId, "PLAYER_ALREADY_PICKED", "Player already selected");
            throw new RuntimeException("Player already picked");
        }
        
        // Validate player exists
        if (!playerRepository.existsById(playerId)) {
            sendError(roomId, "PLAYER_NOT_FOUND", "Player not found");
            throw new RuntimeException("Player not found: " + playerId);
        }
        
        // Check user's roster limit
        RoomParticipant participant = participantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> {
                    sendError(roomId, "PARTICIPANT_NOT_FOUND", "Participant not found");
                    return new RuntimeException("Participant not found");
                });
        
        if (participant.getSelectedPlayerIds().size() >= participant.getRosterSizeLimit()) {
            sendError(roomId, "ROSTER_LIMIT_REACHED", "Roster limit reached");
            throw new RuntimeException("Roster limit reached");
        }
        
        // Atomik update ile pick yap
        long pickNo = draftPickRepository.countByRoomId(roomId) + 1;
        Long currentVersion = room.getVersion();
        
        Query query = new Query(Criteria.where("_id").is(roomId)
                .and("status").is(RoomStatus.DRAFTING)
                .and("currentPickIndex").is(room.getCurrentPickIndex())
                .and("version").is(currentVersion));
        
        Update update = new Update()
                .inc("currentPickIndex", 1)
                .inc("version", 1);
        
        Room updatedRoom = mongoTemplate.findAndModify(query, update, Room.class);
        
        if (updatedRoom == null) {
            log.warn("[room:{}] Concurrent modification detected", roomId);
            sendError(roomId, "CONCURRENT_MODIFICATION", "Concurrent modification detected");
            throw new RuntimeException("Concurrent modification");
        }
        
        // Create draft pick
        DraftPick draftPick = DraftPick.builder()
                .id(UUID.randomUUID().toString())
                .roomId(roomId)
                .userId(userId)
                .playerId(playerId)
                .pickNo((int) pickNo)
                .build();
        
        draftPickRepository.save(draftPick);
        
        // Update participant's selected players
        participant.getSelectedPlayerIds().add(playerId);
        participantRepository.save(participant);
        
        log.info("[room:{}] Pick {} made by user {} for player {}", roomId, pickNo, userId, playerId);
        
        // Check if draft is complete
        updatedRoom = roomRepository.findById(roomId).orElse(updatedRoom);
        boolean draftComplete = checkIfDraftComplete(updatedRoom);
        
        if (draftComplete) {
            updatedRoom.setStatus(RoomStatus.DONE);
            roomRepository.save(updatedRoom);
            log.info("[room:{}] Draft completed", roomId);
        }
        
        // Reset timer for next pick
        timerService.resetTimer(roomId);
        
        // Broadcast pick made event
        PickMadeEvent event = PickMadeEvent.builder()
                .roomId(roomId)
                .userId(userId)
                .playerId(playerId)
                .pickNo((int) pickNo)
                .currentPickIndex(updatedRoom.getCurrentPickIndex())
                .nextUserId(getNextUserId(updatedRoom))
                .build();
        
        webSocketService.broadcastPickMade(roomId, event);
        
        // If draft complete, broadcast room update
        if (draftComplete) {
            RoomDto roomDto = toRoomDtoWithParticipants(updatedRoom);
            webSocketService.broadcastRoomUpdate(roomId, roomDto);
        }
    }
    
    private boolean checkIfDraftComplete(Room room) {
        List<RoomParticipant> participants = participantRepository.findByRoomId(room.getId());
        int totalPicksNeeded = participants.stream()
                .mapToInt(p -> p.getRosterSizeLimit())
                .sum();
        
        long currentPicks = draftPickRepository.countByRoomId(room.getId());
        return currentPicks >= totalPicksNeeded;
    }
    
    private String getNextUserId(Room room) {
        if (room.getCurrentPickIndex() >= room.getPickOrder().size()) {
            return null;
        }
        return room.getPickOrder().get(room.getCurrentPickIndex());
    }
    
    private RoomDto toRoomDtoWithParticipants(Room room) {
        List<RoomParticipant> participants = participantRepository.findByRoomId(room.getId());
        RoomDto dto = dtoMapper.toRoomDto(room);
        dto.setParticipants(dtoMapper.toRoomParticipantDtoList(participants));
        return dto;
    }
    
    private void sendError(String roomId, String reason, String message) {
        ErrorEvent error = ErrorEvent.builder()
                .reason(reason)
                .message(message)
                .build();
        webSocketService.broadcastError(roomId, error);
    }
}


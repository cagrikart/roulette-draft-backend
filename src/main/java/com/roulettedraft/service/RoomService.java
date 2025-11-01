package com.roulettedraft.service;

import com.roulettedraft.domain.model.Room;
import com.roulettedraft.domain.model.RoomParticipant;
import com.roulettedraft.domain.model.RoomStatus;
import com.roulettedraft.dto.CreateRoomRequest;
import com.roulettedraft.dto.JoinRoomRequest;
import com.roulettedraft.dto.RoomDto;
import com.roulettedraft.dto.RoomParticipantDto;
import com.roulettedraft.mapper.DtoMapper;
import com.roulettedraft.repository.RoomParticipantRepository;
import com.roulettedraft.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final DtoMapper dtoMapper;
    private final WebSocketService webSocketService;
    private final TimerService timerService;

    @Transactional
    public RoomDto createRoom(CreateRoomRequest request) {
        log.info("Creating room: {}", request.getName());
        
        // Validate max participants (2-5)
        int maxParticipants = request.getMaxParticipants() != null ? request.getMaxParticipants() : 5;
        if (maxParticipants < 2 || maxParticipants > 5) {
            throw new RuntimeException("Max participants must be between 2 and 5");
        }
        
        Room room = Room.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .status(RoomStatus.WAITING)
                .formation(request.getFormation())
                .maxParticipants(maxParticipants)
                .pickOrder(new ArrayList<>())
                .currentPickIndex(0)
                .build();
        
        room = roomRepository.save(room);
        log.info("Room created with id: {}", room.getId());
        
        RoomDto roomDto = toRoomDtoWithParticipants(room);
        webSocketService.broadcastRoomUpdate(room.getId(), roomDto);
        
        return roomDto;
    }

    @Transactional
    public RoomDto joinRoom(String roomId, JoinRoomRequest request) {
        log.info("User {} joining room {}", request.getUserId(), roomId);
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        
        if (room.getStatus() != RoomStatus.WAITING) {
            throw new RuntimeException("Cannot join room in status: " + room.getStatus());
        }
        
        // Check if user already joined
        participantRepository.findByRoomIdAndUserId(roomId, request.getUserId())
                .ifPresent(p -> {
                    log.debug("User {} already in room {}", request.getUserId(), roomId);
                    return;
                });
        
        // Check max participants (2-5)
        List<RoomParticipant> existing = participantRepository.findByRoomId(roomId);
        if (existing.size() >= room.getMaxParticipants()) {
            throw new RuntimeException("Room is full. Max participants: " + room.getMaxParticipants());
        }
        
        RoomParticipant participant = RoomParticipant.builder()
                .id(UUID.randomUUID().toString())
                .roomId(roomId)
                .userId(request.getUserId())
                .displayName(request.getDisplayName())
                .rosterSizeLimit(request.getRosterSizeLimit() != null ? request.getRosterSizeLimit() : 11)
                .build();
        
        participantRepository.save(participant);
        log.info("User {} joined room {}", request.getUserId(), roomId);
        
        RoomDto roomDto = toRoomDtoWithParticipants(room);
        webSocketService.broadcastRoomUpdate(roomId, roomDto);
        
        return roomDto;
    }

    @Transactional
    public RoomDto startDraft(String roomId) {
        log.info("Starting draft for room: {}", roomId);
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        
        if (room.getStatus() != RoomStatus.WAITING) {
            throw new RuntimeException("Room is not in WAITING status");
        }
        
        List<RoomParticipant> participants = participantRepository.findByRoomId(roomId);
        if (participants.size() < 2) {
            throw new RuntimeException("Need at least 2 participants to start. Current: " + participants.size());
        }
        if (participants.size() > 5) {
            throw new RuntimeException("Too many participants. Max: 5, Current: " + participants.size());
        }
        
        // Create pick order from participant user IDs
        List<String> pickOrder = new ArrayList<>();
        for (RoomParticipant p : participants) {
            pickOrder.add(p.getUserId());
        }
        Collections.shuffle(pickOrder);
        
        room.setStatus(RoomStatus.DRAFTING);
        room.setPickOrder(pickOrder);
        room.setCurrentPickIndex(0);
        
        room = roomRepository.save(room);
        log.info("Draft started for room {}, pick order: {}", roomId, pickOrder);
        
        // Start timer for first pick
        timerService.resetTimer(roomId);
        
        RoomDto roomDto = toRoomDtoWithParticipants(room);
        webSocketService.broadcastRoomUpdate(roomId, roomDto);
        
        return roomDto;
    }

    public RoomDto getRoomById(String roomId) {
        log.debug("Fetching room: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        return toRoomDtoWithParticipants(room);
    }

    @Transactional
    public RoomDto updateParticipantSelectedTeams(String roomId, String userId, List<String> selectedTeams) {
        log.info("🔵 [updateParticipantSelectedTeams] Updating selected teams for user {} in room {} with teams: {}", userId, roomId, selectedTeams);
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        
        RoomParticipant participant = participantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new RuntimeException("Participant not found in room: " + roomId));
        
        log.info("🔵 [updateParticipantSelectedTeams] Found participant: id={}, displayName={}, current selectedTeams={}", 
            participant.getId(), participant.getDisplayName(), participant.getSelectedTeams());
        
        // selectedTeams null değilse, yeni ArrayList oluştur
        List<String> teamsToSave = (selectedTeams != null && !selectedTeams.isEmpty()) 
            ? new ArrayList<>(selectedTeams) 
            : new ArrayList<>();
        
        log.info("🔵 [updateParticipantSelectedTeams] Teams to save: {}", teamsToSave);
        
        participant.setSelectedTeams(teamsToSave);
        participant = participantRepository.save(participant);
        
        log.info("🔵 [updateParticipantSelectedTeams] After save - participant.selectedTeams: {}", participant.getSelectedTeams());
        
        // MongoDB'den tekrar oku ve kontrol et
        RoomParticipant savedParticipant = participantRepository.findById(participant.getId())
            .orElseThrow(() -> new RuntimeException("Participant not found after save"));
        
        log.info("🔵 [updateParticipantSelectedTeams] Re-read from MongoDB - savedParticipant.selectedTeams: {}", savedParticipant.getSelectedTeams());
        log.info("🔵 [updateParticipantSelectedTeams] Updated selected teams for user {}: {}", userId, selectedTeams);
        
        // Repository'den tekrar oku (fresh data için)
        participant = participantRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new RuntimeException("Participant not found after save"));
        
        log.info("🔵 [updateParticipantSelectedTeams] Re-read from DB - participant.selectedTeams: {}", participant.getSelectedTeams());
        
        RoomDto roomDto = toRoomDtoWithParticipants(room);
        
        // Debug: RoomDto'daki participants'ı kontrol et
        log.info("🔵 [updateParticipantSelectedTeams] RoomDto participants after mapping:");
        if (roomDto.getParticipants() != null) {
            for (RoomParticipantDto p : roomDto.getParticipants()) {
                log.info("🔵   Participant {} (userId: {}): selectedTeams = {}", 
                    p.getDisplayName(), p.getUserId(), p.getSelectedTeams());
            }
        } else {
            log.warn("🔵 [updateParticipantSelectedTeams] RoomDto.getParticipants() is NULL!");
        }
        
        webSocketService.broadcastRoomUpdate(roomId, roomDto);
        
        return roomDto;
    }

    private RoomDto toRoomDtoWithParticipants(Room room) {
        List<RoomParticipant> participants = participantRepository.findByRoomId(room.getId());
        
        log.info("🔵 [toRoomDtoWithParticipants] Found {} participants from DB:", participants.size());
        for (RoomParticipant p : participants) {
            log.info("🔵   Participant {} (userId: {}): selectedTeams = {} (type: {}, isNull: {})", 
                p.getDisplayName(), 
                p.getUserId(), 
                p.getSelectedTeams(),
                p.getSelectedTeams() != null ? p.getSelectedTeams().getClass().getName() : "null",
                p.getSelectedTeams() == null);
        }
        
        RoomDto dto = dtoMapper.toRoomDto(room);
        List<RoomParticipantDto> participantDtos = dtoMapper.toRoomParticipantDtoList(participants);
        
        log.info("🔵 [toRoomDtoWithParticipants] After MapStruct mapping:");
        for (RoomParticipantDto pd : participantDtos) {
            log.info("🔵   Participant DTO {} (userId: {}): selectedTeams = {} (type: {}, isNull: {})", 
                pd.getDisplayName(), 
                pd.getUserId(), 
                pd.getSelectedTeams(),
                pd.getSelectedTeams() != null ? pd.getSelectedTeams().getClass().getName() : "null",
                pd.getSelectedTeams() == null);
            
            // Eğer null ise, boş liste olarak set et
            if (pd.getSelectedTeams() == null) {
                pd.setSelectedTeams(new ArrayList<>());
                log.warn("🔵   ⚠️ Participant DTO {} selectedTeams NULL'dı, boş liste olarak set edildi!", pd.getDisplayName());
            }
        }
        
        dto.setParticipants(participantDtos);
        return dto;
    }
}


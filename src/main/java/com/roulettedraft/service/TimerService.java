package com.roulettedraft.service;

import com.roulettedraft.domain.model.Room;
import com.roulettedraft.domain.model.RoomStatus;
import com.roulettedraft.dto.websocket.TimerTickEvent;
import com.roulettedraft.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimerService {
    private final RoomRepository roomRepository;
    private final WebSocketService webSocketService;
    private final ConcurrentHashMap<String, Integer> roomTimers = new ConcurrentHashMap<>();
    private static final int PICK_TIMEOUT_SECONDS = 30;

    @Scheduled(fixedRate = 1000) // Her saniye
    public void tickTimers() {
        // Sadece aktif timer'ı olan odaları işle (tüm DRAFTING odalarını sorgulamayalım)
        if (roomTimers.isEmpty()) {
            return; // Timer yoksa hiç sorgu yapma
        }
        
        // Aktif timer'ı olan room ID'lerini al
        List<String> activeRoomIds = new ArrayList<>(roomTimers.keySet());
        
        // Sadece bu odaları sorgula (daha efficient)
        List<Room> draftingRooms = roomRepository.findByIdInAndStatus(activeRoomIds, RoomStatus.DRAFTING);
        
        for (Room room : draftingRooms) {
            String roomId = room.getId();
            Integer remaining = roomTimers.getOrDefault(roomId, PICK_TIMEOUT_SECONDS);
            
            if (remaining > 0) {
                remaining--;
                roomTimers.put(roomId, remaining);
                
                String currentUserId = getCurrentUserId(room);
                TimerTickEvent event = TimerTickEvent.builder()
                        .roomId(roomId)
                        .remainingSeconds(remaining)
                        .currentUserId(currentUserId)
                        .build();
                
                webSocketService.broadcastTimerTick(roomId, event);
                
                if (remaining == 0) {
                    log.warn("[room:{}] Timer expired for user {}", roomId, currentUserId);
                    // Timer expired - could implement auto-pick or skip logic here
                    roomTimers.remove(roomId);
                }
            }
        }
        
        // Aktif timer olmayan ama hala DRAFTING olan odaları temizle
        for (String roomId : activeRoomIds) {
            if (!draftingRooms.stream().anyMatch(r -> r.getId().equals(roomId))) {
                roomTimers.remove(roomId);
                log.debug("[room:{}] Removed timer - room no longer in DRAFTING status", roomId);
            }
        }
    }
    
    public void resetTimer(String roomId) {
        roomTimers.put(roomId, PICK_TIMEOUT_SECONDS);
        log.debug("[room:{}] Timer reset", roomId);
    }
    
    private String getCurrentUserId(Room room) {
        if (room.getPickOrder() == null || room.getCurrentPickIndex() >= room.getPickOrder().size()) {
            return null;
        }
        return room.getPickOrder().get(room.getCurrentPickIndex());
    }
}


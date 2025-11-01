package com.roulettedraft.service;

import com.roulettedraft.dto.RoomDto;
import com.roulettedraft.dto.websocket.ErrorEvent;
import com.roulettedraft.dto.websocket.PickMadeEvent;
import com.roulettedraft.dto.websocket.RoomUpdatedEvent;
import com.roulettedraft.dto.websocket.TimerTickEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<org.springframework.web.socket.WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    
    public void registerSession(String roomId, org.springframework.web.socket.WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.debug("[room:{}] Session registered, total: {}", roomId, roomSessions.get(roomId).size());
    }
    
    public void unregisterSession(String roomId, org.springframework.web.socket.WebSocketSession session) {
        CopyOnWriteArraySet<org.springframework.web.socket.WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
            log.debug("[room:{}] Session unregistered, remaining: {}", roomId, sessions.size());
        }
    }
    
    public void broadcastRoomUpdate(String roomId, RoomDto room) {
        RoomUpdatedEvent event = RoomUpdatedEvent.builder()
                .room(room)
                .build();
        broadcast(roomId, event);
    }
    
    public void broadcastPickMade(String roomId, PickMadeEvent event) {
        broadcast(roomId, event);
    }
    
    public void broadcastError(String roomId, ErrorEvent error) {
        broadcast(roomId, error);
    }
    
    public void broadcastTimerTick(String roomId, TimerTickEvent event) {
        broadcast(roomId, event);
    }
    
    private void broadcast(String roomId, Object event) {
        CopyOnWriteArraySet<org.springframework.web.socket.WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        
        try {
            String message = objectMapper.writeValueAsString(event);
            sessions.removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                        return false;
                    }
                    return true;
                } catch (IOException e) {
                    log.error("[room:{}] Error sending message to session", roomId, e);
                    return true;
                }
            });
            
            log.debug("[room:{}] Broadcasted {} to {} sessions", roomId, event.getClass().getSimpleName(), sessions.size());
        } catch (Exception e) {
            log.error("[room:{}] Error broadcasting message", roomId, e);
        }
    }
}


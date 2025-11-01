package com.roulettedraft.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roulettedraft.dto.websocket.PickPlayerRequest;
import com.roulettedraft.dto.websocket.SubscribeRoomRequest;
import com.roulettedraft.service.DraftService;
import com.roulettedraft.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final WebSocketService webSocketService;
    private final DraftService draftService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            log.debug("Received message: {}", payload);

            // Parse message type
            if (payload.contains("\"type\"")) {
                if (payload.contains("\"SUBSCRIBE_ROOM\"")) {
                    handleSubscribeRoom(session, payload);
                } else if (payload.contains("\"PICK_PLAYER\"")) {
                    handlePickPlayer(session, payload);
                } else {
                    log.warn("Unknown message type: {}", payload);
                }
            } else {
                log.warn("Invalid message format: {}", payload);
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
            webSocketService.broadcastError(null, 
                com.roulettedraft.dto.websocket.ErrorEvent.builder()
                    .reason("PROCESSING_ERROR")
                    .message("Error processing message: " + e.getMessage())
                    .build());
        }
    }

    private void handleSubscribeRoom(WebSocketSession session, String payload) throws Exception {
        SubscribeRoomRequest request = objectMapper.readValue(payload, SubscribeRoomRequest.class);
        String roomId = request.getRoomId();
        
        MDC.put("roomId", roomId);
        log.info("[room:{}] User {} subscribing", roomId, request.getUserId());
        
        webSocketService.registerSession(roomId, session);
        
        // Store roomId in session attributes for cleanup
        session.getAttributes().put("roomId", roomId);
    }

    private void handlePickPlayer(WebSocketSession session, String payload) throws Exception {
        PickPlayerRequest request = objectMapper.readValue(payload, PickPlayerRequest.class);
        String roomId = request.getRoomId();
        String userId = request.getUserId();
        String playerId = request.getPlayerId();
        
        MDC.put("roomId", roomId);
        log.info("[room:{}] User {} picking player {}", roomId, userId, playerId);
        
        try {
            draftService.makePick(roomId, userId, playerId);
        } catch (Exception e) {
            log.error("[room:{}] Error making pick", roomId, e);
            webSocketService.broadcastError(roomId, 
                com.roulettedraft.dto.websocket.ErrorEvent.builder()
                    .reason("PICK_FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = (String) session.getAttributes().get("roomId");
        if (roomId != null) {
            MDC.put("roomId", roomId);
            log.info("[room:{}] WebSocket connection closed: {}", roomId, session.getId());
            webSocketService.unregisterSession(roomId, session);
            MDC.clear();
        } else {
            log.info("WebSocket connection closed: {}", session.getId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String roomId = (String) session.getAttributes().get("roomId");
        if (roomId != null) {
            MDC.put("roomId", roomId);
            log.error("[room:{}] WebSocket transport error", roomId, exception);
            webSocketService.unregisterSession(roomId, session);
            MDC.clear();
        } else {
            log.error("WebSocket transport error", exception);
        }
    }
}


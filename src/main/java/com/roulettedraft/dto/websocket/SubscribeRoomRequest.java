package com.roulettedraft.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRoomRequest {
    private String type = "SUBSCRIBE_ROOM";
    private String roomId;
    private String userId;
}

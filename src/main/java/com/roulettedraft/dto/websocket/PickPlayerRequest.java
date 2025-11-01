package com.roulettedraft.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickPlayerRequest {
    private String type = "PICK_PLAYER";
    private String roomId;
    private String userId;
    private String playerId;
}

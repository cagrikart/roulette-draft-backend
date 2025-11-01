package com.roulettedraft.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickMadeEvent {
    private String type = "PICK_MADE";
    private String roomId;
    private String userId;
    private String playerId;
    private Integer pickNo;
    private Integer currentPickIndex;
    private String nextUserId;
}

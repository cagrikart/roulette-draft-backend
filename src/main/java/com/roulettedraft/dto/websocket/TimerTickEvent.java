package com.roulettedraft.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimerTickEvent {
    private String type = "TIMER_TICK";
    private String roomId;
    private Integer remainingSeconds;
    private String currentUserId;
}

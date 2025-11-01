package com.roulettedraft.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorEvent {
    private String type = "ERROR";
    private String reason;
    private String message;
}

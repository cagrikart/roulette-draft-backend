package com.roulettedraft.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Display name is required")
    private String displayName;
    
    private Integer rosterSizeLimit;
}


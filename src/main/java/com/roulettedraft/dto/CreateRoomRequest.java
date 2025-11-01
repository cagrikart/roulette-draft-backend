package com.roulettedraft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    @NotBlank(message = "Room name is required")
    private String name;
    
    private String formation;
    
    @Min(value = 2, message = "Max participants must be at least 2")
    @Max(value = 5, message = "Max participants must be at most 5")
    private Integer maxParticipants;
}


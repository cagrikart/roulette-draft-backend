package com.roulettedraft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomParticipantDto {
    private String id;
    private String roomId;
    private String userId;
    private String displayName;
    private List<String> selectedPlayerIds;
    private List<String> selectedTeams; // Oyuncunun seçtiği takımlar
    private Integer rosterSizeLimit;
    private LocalDateTime createdAt;
}


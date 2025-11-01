package com.roulettedraft.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RandomFillRequest {
    @NotEmpty(message = "Selected teams cannot be empty")
    private List<String> selectedTeams;
    
    @NotEmpty(message = "Squads cannot be empty")
    private List<SquadConfig> squads;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SquadConfig {
        private String id;
        private String name;
        private String formation;
        private Map<String, String> players; // slotId -> playerId (mevcut oyuncular)
        private List<String> bench; // bench playerIds
    }
}


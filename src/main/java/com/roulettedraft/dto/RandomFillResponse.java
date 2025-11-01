package com.roulettedraft.dto;

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
public class RandomFillResponse {
    private List<FilledSquad> squads;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilledSquad {
        private String id;
        private String name;
        private String formation;
        private Map<String, String> players; // slotId -> playerId
        private List<String> bench; // bench playerIds
    }
}


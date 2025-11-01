package com.roulettedraft.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSelectedTeamsRequest {
    @NotEmpty
    private String userId;
    
    @NotEmpty
    private List<String> selectedTeams;
}


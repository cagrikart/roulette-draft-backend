package com.roulettedraft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDto {
    private String id;
    private String name;
    private String league; // Optional, can be empty
    private String logoUrl; // Optional
}


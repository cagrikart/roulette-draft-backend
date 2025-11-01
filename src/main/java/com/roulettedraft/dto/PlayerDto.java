package com.roulettedraft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto {
    private String id;
    private String team;
    private String name;
    private String position;
    private String nationality;
    private String marketValue;
    private String league;
}

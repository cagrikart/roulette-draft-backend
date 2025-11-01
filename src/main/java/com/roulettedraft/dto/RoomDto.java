package com.roulettedraft.dto;

import com.roulettedraft.domain.model.RoomStatus;
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
public class RoomDto {
    private String id;
    private String name;
    private RoomStatus status;
    private String formation;
    private Integer maxParticipants;
    private List<String> pickOrder;
    private Integer currentPickIndex;
    private LocalDateTime createdAt;
    private List<RoomParticipantDto> participants;
}


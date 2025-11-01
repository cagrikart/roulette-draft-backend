package com.roulettedraft.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    private String id;
    
    private String name;
    
    @Indexed
    @Builder.Default
    private RoomStatus status = RoomStatus.WAITING;
    
    private String formation;
    
    private Integer maxParticipants;
    
    @Builder.Default
    private List<String> pickOrder = new ArrayList<>();
    
    @Builder.Default
    private Integer currentPickIndex = 0;
    
    @Version
    private Long version;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}


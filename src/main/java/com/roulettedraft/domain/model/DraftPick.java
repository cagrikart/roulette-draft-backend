package com.roulettedraft.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "draft_picks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "roomId_playerId_unique_idx", def = "{'roomId': 1, 'playerId': 1}", unique = true)
public class DraftPick {
    @Id
    private String id;
    
    @Indexed
    private String roomId;
    
    private String userId;
    
    private String playerId;
    
    private Integer pickNo;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}


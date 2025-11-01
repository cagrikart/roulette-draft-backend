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
import java.util.ArrayList;
import java.util.List;

@Document(collection = "room_participants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "roomId_userId_idx", def = "{'roomId': 1, 'userId': 1}")
public class RoomParticipant {
    @Id
    private String id;
    
    @Indexed
    private String roomId;
    
    private String userId;
    
    private String displayName;
    
    @Builder.Default
    private List<String> selectedPlayerIds = new ArrayList<>();
    
    @Builder.Default
    private List<String> selectedTeams = new ArrayList<>(); // Oyuncunun seçtiği takımlar
    
    private Integer rosterSizeLimit;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}


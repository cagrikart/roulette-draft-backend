package com.roulettedraft.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "roulette-draft")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "team_name_idx", def = "{'team': 1, 'name': 1}")
public class Player {
    @Id
    private String id;
    
    @Field("team")
    @Indexed
    private String team;
    
    @Field("name")
    private String name;
    
    private String position;
    
    private String nationality;
    
    @Field("market_value")
    private String marketValue;
    
    @Field("league")
    @Indexed
    private String league; // League bilgisi (örn: "superLig", "serieA")
}


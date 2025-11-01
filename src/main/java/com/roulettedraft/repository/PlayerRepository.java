package com.roulettedraft.repository;

import com.roulettedraft.domain.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends MongoRepository<Player, String> {
    List<Player> findByTeamIgnoreCase(String team);
    List<Player> findByLeagueIgnoreCase(String league);
    List<Player> findByIdIn(List<String> ids);
}


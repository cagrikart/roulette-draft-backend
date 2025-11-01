package com.roulettedraft.repository;

import com.roulettedraft.domain.model.DraftPick;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DraftPickRepository extends MongoRepository<DraftPick, String> {
    boolean existsByRoomIdAndPlayerId(String roomId, String playerId);
    long countByRoomId(String roomId);
}


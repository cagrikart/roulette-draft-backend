package com.roulettedraft.repository;

import com.roulettedraft.domain.model.RoomParticipant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomParticipantRepository extends MongoRepository<RoomParticipant, String> {
    List<RoomParticipant> findByRoomId(String roomId);
    Optional<RoomParticipant> findByRoomIdAndUserId(String roomId, String userId);
}


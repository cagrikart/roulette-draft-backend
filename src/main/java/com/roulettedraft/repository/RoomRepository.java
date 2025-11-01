package com.roulettedraft.repository;

import com.roulettedraft.domain.model.Room;
import com.roulettedraft.domain.model.RoomStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findByStatus(RoomStatus status);
    
    // TimerService için optimize edilmiş query - sadece aktif timer'ı olan odaları sorgula
    List<Room> findByIdInAndStatus(List<String> roomIds, RoomStatus status);
}


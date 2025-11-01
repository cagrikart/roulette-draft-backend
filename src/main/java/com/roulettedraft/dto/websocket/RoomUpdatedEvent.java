package com.roulettedraft.dto.websocket;

import com.roulettedraft.dto.RoomDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdatedEvent {
    private String type = "ROOM_UPDATED";
    private RoomDto room;
}

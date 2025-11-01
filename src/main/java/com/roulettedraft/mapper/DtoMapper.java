package com.roulettedraft.mapper;

import com.roulettedraft.domain.model.Player;
import com.roulettedraft.domain.model.Room;
import com.roulettedraft.domain.model.RoomParticipant;
import com.roulettedraft.dto.PlayerDto;
import com.roulettedraft.dto.RoomDto;
import com.roulettedraft.dto.RoomParticipantDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DtoMapper {
    DtoMapper INSTANCE = Mappers.getMapper(DtoMapper.class);

    PlayerDto toPlayerDto(Player player);
    List<PlayerDto> toPlayerDtoList(List<Player> players);

    RoomDto toRoomDto(Room room);
    
    // selectedTeams field'ını açıkça map et
    @Mapping(source = "selectedTeams", target = "selectedTeams")
    RoomParticipantDto toRoomParticipantDto(RoomParticipant participant);
    
    List<RoomParticipantDto> toRoomParticipantDtoList(List<RoomParticipant> participants);
}


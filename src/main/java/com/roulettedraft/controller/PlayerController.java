package com.roulettedraft.controller;

import com.roulettedraft.dto.PlayerDto;
import com.roulettedraft.domain.model.Player;
import com.roulettedraft.mapper.DtoMapper;
import com.roulettedraft.repository.PlayerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Tag(name = "Player API", description = "Player management endpoints")
public class PlayerController {
    private final PlayerRepository playerRepository;
    private final DtoMapper dtoMapper;

    @GetMapping
    @Operation(summary = "Get all players from database")
    public ResponseEntity<List<PlayerDto>> getAllPlayers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Player> playersPage = playerRepository.findAll(pageable);
            return ResponseEntity.ok(dtoMapper.toPlayerDtoList(playersPage.getContent()));
        }
        return ResponseEntity.ok(dtoMapper.toPlayerDtoList(playerRepository.findAll()));
    }

    @GetMapping("/team/{teamName}")
    @Operation(summary = "Get players by team name")
    public ResponseEntity<List<PlayerDto>> getPlayersByTeamName(@PathVariable String teamName) {
        return ResponseEntity.ok(dtoMapper.toPlayerDtoList(playerRepository.findByTeamIgnoreCase(teamName)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player by ID")
    public ResponseEntity<PlayerDto> getPlayerById(@PathVariable String id) {
        Optional<Player> player = playerRepository.findById(id);
        if (player.isPresent()) {
            return ResponseEntity.ok(dtoMapper.toPlayerDto(player.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search players by various criteria")
    public ResponseEntity<List<PlayerDto>> searchPlayers(
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String nationality) {
        
        List<Player> allPlayers = playerRepository.findAll();
        
        List<Player> filtered = allPlayers.stream()
                .filter(p -> team == null || (p.getTeam() != null && p.getTeam().equalsIgnoreCase(team)))
                .filter(p -> position == null || (p.getPosition() != null && p.getPosition().equalsIgnoreCase(position)))
                .filter(p -> nationality == null || (p.getNationality() != null && p.getNationality().equalsIgnoreCase(nationality)))
                .toList();
        
        return ResponseEntity.ok(dtoMapper.toPlayerDtoList(filtered));
    }
}

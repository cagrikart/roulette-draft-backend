package com.roulettedraft.controller;

import com.roulettedraft.domain.model.Player;
import com.roulettedraft.dto.TeamDto;
import com.roulettedraft.repository.PlayerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Team API", description = "Team management endpoints")
public class TeamController {
    private final PlayerRepository playerRepository;

    @GetMapping
    @Operation(summary = "Get all unique teams from database, optionally filtered by league")
    public ResponseEntity<List<TeamDto>> getAllTeams(
            @RequestParam(required = false, name = "league") String leagueParam) {
        
        // URL decode ve trim yap
        String league = leagueParam != null ? leagueParam.trim() : null;
        
        log.info("Getting teams with league filter: '{}'", league);
        
        // Repository'den çek (MongoTemplate kullanmıyoruz)
        List<Player> players;
        if (league != null && !league.isEmpty()) {
            // League parametresi varsa, repository method ile filtrele
            players = playerRepository.findByLeagueIgnoreCase(league);
            log.info("Found {} players with league '{}' using repository", players.size(), league);
        } else {
            // Yoksa tüm player'ları repository ile al
            players = playerRepository.findAll();
            log.info("Total players in DB: {}", players.size());
        }
        
        // Unique team isimlerini çıkar ve TeamDto listesine dönüştür
        Set<String> uniqueTeamNames = players.stream()
                .map(Player::getTeam)
                .filter(team -> team != null && !team.isEmpty())
                .collect(Collectors.toSet());
        
        log.info("Unique teams found: {}", uniqueTeamNames.size());
        
        // Her takım için league bilgisini bul
        List<Player> finalPlayers = players;
        List<TeamDto> teams = uniqueTeamNames.stream()
                .map(teamName -> {
                    // Bu takımdan bir player bul ve league bilgisini al
                    String teamLeague = finalPlayers.stream()
                            .filter(p -> teamName.equals(p.getTeam()))
                            .map(Player::getLeague)
                            .filter(l -> l != null && !l.isEmpty())
                            .findFirst()
                            .orElse("");
                    
                    return TeamDto.builder()
                            .id(teamName) // Team name'i ID olarak kullan
                            .name(teamName)
                            .league(teamLeague)
                            .build();
                })
                .sorted((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName())) // Alfabetik sırala
                .collect(Collectors.toList());
        
        log.info("Returning {} teams", teams.size());
        
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{teamName}/players")
    @Operation(summary = "Get players by team name")
    public ResponseEntity<List<Player>> getPlayersByTeamName(@PathVariable String teamName) {
        // Repository method ile çek
        return ResponseEntity.ok(playerRepository.findByTeamIgnoreCase(teamName));
    }
}

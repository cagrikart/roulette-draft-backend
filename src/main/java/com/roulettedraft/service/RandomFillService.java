package com.roulettedraft.service;

import com.roulettedraft.domain.model.Player;
import com.roulettedraft.dto.RandomFillRequest;
import com.roulettedraft.dto.RandomFillResponse;
import com.roulettedraft.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RandomFillService {
    private final PlayerRepository playerRepository;
    
    // Formation slot mappings (frontend'deki formationLayouts ile aynı)
    private static final Map<String, List<String>> FORMATION_SLOTS = Map.of(
        "4-3-3", List.of("GK", "LB", "LCB", "RCB", "RB", "LCM", "CM", "RCM", "LW", "ST", "RW"),
        "4-4-2", List.of("GK", "LB", "LCB", "RCB", "RB", "LM", "LCM", "RCM", "RM", "LST", "RST"),
        "3-5-2", List.of("GK", "LCB", "CB", "RCB", "LWB", "CDM", "RWB", "LCM", "RCM", "LST", "RST"),
        "4-2-3-1", List.of("GK", "LB", "LCB", "RCB", "RB", "CDM", "CDM2", "CAM", "LW", "ST", "RW"),
        "4-1-4-1", List.of("GK", "LB", "LCB", "RCB", "RB", "CDM", "LM", "LCM", "RCM", "RM", "ST"),
        "5-3-2", List.of("GK", "LWB", "LCB", "CB", "RCB", "RWB", "LCM", "CDM", "RCM", "LST", "RST"),
        "5-2-3", List.of("GK", "LWB", "LCB", "CB", "RCB", "RWB", "LCM", "RCM", "LW", "ST", "RW"),
        "4-5-1", List.of("GK", "LB", "LCB", "RCB", "RB", "LM", "LCM", "CM", "RCM", "RM", "ST")
    );
    
    public RandomFillResponse randomFillSquads(RandomFillRequest request) {
        log.info("Random filling squads for {} teams and {} squads", request.getSelectedTeams().size(), request.getSquads().size());
        
        // 1. Seçilen takımların tüm oyuncularını çek
        Map<String, List<Player>> playersByTeam = new HashMap<>();
        for (String teamName : request.getSelectedTeams()) {
            List<Player> players = playerRepository.findByTeamIgnoreCase(teamName);
            playersByTeam.put(teamName, players);
            log.info("Team {}: {} players found", teamName, players.size());
        }
        
        // 2. Tüm oyuncuları bir pool'a topla
        List<Player> allPlayers = playersByTeam.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        // Oyuncuları shuffle et (random)
        Collections.shuffle(allPlayers, new Random());
        
        log.info("Total players in pool: {}", allPlayers.size());
        
        // 3. Her squad için slot'ları doldur
        List<RandomFillResponse.FilledSquad> filledSquads = new ArrayList<>();
        Set<String> usedPlayerIds = new HashSet<>();
        
        for (RandomFillRequest.SquadConfig squadConfig : request.getSquads()) {
            log.info("Filling squad: {} ({})", squadConfig.getName(), squadConfig.getFormation());
            
            // Mevcut oyuncuları al
            Map<String, String> filledPlayers = new HashMap<>(squadConfig.getPlayers() != null ? squadConfig.getPlayers() : Map.of());
            List<String> bench = new ArrayList<>(squadConfig.getBench() != null ? squadConfig.getBench() : List.of());
            
            // Kullanılan oyuncuları işaretle
            filledPlayers.values().forEach(usedPlayerIds::add);
            bench.forEach(usedPlayerIds::add);
            
            // Formation slot'larını al
            List<String> requiredSlots = FORMATION_SLOTS.getOrDefault(squadConfig.getFormation(), FORMATION_SLOTS.get("4-3-3"));
            
            // Eksik slot'ları bul
            List<String> neededSlots = requiredSlots.stream()
                .filter(slot -> !filledPlayers.containsKey(slot))
                .collect(Collectors.toList());
            
            log.info("Squad {} needs {} slots filled", squadConfig.getName(), neededSlots.size());
            
            // Slot'ları shuffle et
            Collections.shuffle(neededSlots);
            
            // Her slot için uygun oyuncu bul
            for (String slotId : neededSlots) {
                Player selectedPlayer = findMatchingPlayer(slotId, allPlayers, usedPlayerIds);
                
                if (selectedPlayer != null) {
                    filledPlayers.put(slotId, selectedPlayer.getId());
                    usedPlayerIds.add(selectedPlayer.getId());
                    log.debug("  Slot {} -> Player {} ({})", slotId, selectedPlayer.getName(), selectedPlayer.getPosition());
                } else {
                    log.warn("  No available player found for slot {}", slotId);
                }
            }
            
            // Bench'i de doldur (opsiyonel - şimdilik boş bırakıyoruz)
            
            filledSquads.add(RandomFillResponse.FilledSquad.builder()
                .id(squadConfig.getId())
                .name(squadConfig.getName())
                .formation(squadConfig.getFormation())
                .players(filledPlayers)
                .bench(bench)
                .build());
        }
        
        log.info("Random fill completed. Filled {} squads", filledSquads.size());
        
        return RandomFillResponse.builder()
            .squads(filledSquads)
            .build();
    }
    
    private Player findMatchingPlayer(String slotId, List<Player> availablePlayers, Set<String> usedPlayerIds) {
        // Önce pozisyona uygun oyuncuları bul
        List<Player> matchingPlayers = availablePlayers.stream()
            .filter(p -> !usedPlayerIds.contains(p.getId()))
            .filter(p -> positionMatches(slotId, p.getPosition()))
            .collect(Collectors.toList());
        
        if (!matchingPlayers.isEmpty()) {
            // Random bir tane seç
            Collections.shuffle(matchingPlayers);
            return matchingPlayers.get(0);
        }
        
        // Fallback: Herhangi bir kullanılmamış oyuncu
        List<Player> anyPlayers = availablePlayers.stream()
            .filter(p -> !usedPlayerIds.contains(p.getId()))
            .collect(Collectors.toList());
        
        if (!anyPlayers.isEmpty()) {
            Collections.shuffle(anyPlayers);
            return anyPlayers.get(0);
        }
        
        return null;
    }
    
    private boolean positionMatches(String slotId, String playerPosition) {
        if (playerPosition == null) return false;
        
        String pos = playerPosition.toUpperCase();
        String slot = slotId.toUpperCase();
        
        if (slot.equals("GK")) return pos.equals("GK");
        if (slot.contains("CB")) return pos.contains("CB");
        if (slot.contains("LB")) return pos.equals("LB") || pos.contains("LWB");
        if (slot.contains("RB")) return pos.equals("RB") || pos.contains("RWB");
        if (slot.contains("WB")) return pos.contains("WB") || pos.equals("LB") || pos.equals("RB");
        if (slot.contains("CDM") || slot.contains("DM")) return pos.contains("DM") || pos.contains("CDM");
        if (slot.contains("CM")) return pos.contains("CM") || pos.contains("DM");
        if (slot.contains("CAM") || slot.contains("AM")) return pos.contains("AM") || pos.equals("CAM");
        if (slot.contains("LW")) return pos.equals("LW");
        if (slot.contains("RW")) return pos.equals("RW");
        if (slot.contains("ST")) return pos.equals("ST") || pos.equals("CF");
        if (slot.contains("LM") || slot.contains("RM")) return pos.contains("M");
        
        return true; // Default: any position
    }
}


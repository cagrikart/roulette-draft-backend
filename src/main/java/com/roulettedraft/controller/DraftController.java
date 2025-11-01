package com.roulettedraft.controller;

import com.roulettedraft.dto.RandomFillRequest;
import com.roulettedraft.dto.RandomFillResponse;
import com.roulettedraft.service.RandomFillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/draft")
@RequiredArgsConstructor
@Tag(name = "Draft API", description = "Draft-related endpoints")
public class DraftController {
    private final RandomFillService randomFillService;
    
    @PostMapping("/random-fill")
    @Operation(summary = "Random fill squads with players from selected teams")
    public ResponseEntity<RandomFillResponse> randomFillSquads(@Valid @RequestBody RandomFillRequest request) {
        return ResponseEntity.ok(randomFillService.randomFillSquads(request));
    }
}


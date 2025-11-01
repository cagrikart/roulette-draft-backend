package com.roulettedraft.controller;

import com.roulettedraft.dto.CreateRoomRequest;
import com.roulettedraft.dto.JoinRoomRequest;
import com.roulettedraft.dto.RoomDto;
import com.roulettedraft.dto.UpdateSelectedTeamsRequest;
import com.roulettedraft.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Room API", description = "Room management endpoints")
public class RoomController {
    private final RoomService roomService;

    @PostMapping
    @Operation(summary = "Create a new room (max 2-5 participants)")
    public ResponseEntity<RoomDto> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }

    @PostMapping("/{roomId}/join")
    @Operation(summary = "Join a room")
    public ResponseEntity<RoomDto> joinRoom(@PathVariable String roomId, @Valid @RequestBody JoinRoomRequest request) {
        return ResponseEntity.ok(roomService.joinRoom(roomId, request));
    }

    @PostMapping("/{roomId}/start")
    @Operation(summary = "Start the draft (requires at least 2 participants)")
    public ResponseEntity<RoomDto> startDraft(@PathVariable String roomId) {
        return ResponseEntity.ok(roomService.startDraft(roomId));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "Get room by ID")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable String roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @PostMapping("/{roomId}/participants/{userId}/teams")
    @Operation(summary = "Update participant's selected teams")
    public ResponseEntity<RoomDto> updateParticipantTeams(
            @PathVariable String roomId,
            @PathVariable String userId,
            @Valid @RequestBody UpdateSelectedTeamsRequest request) {
        if (!userId.equals(request.getUserId())) {
            throw new RuntimeException("User ID mismatch");
        }
        return ResponseEntity.ok(roomService.updateParticipantSelectedTeams(roomId, userId, request.getSelectedTeams()));
    }
}


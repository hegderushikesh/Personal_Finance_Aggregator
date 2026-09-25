package com.finpilot.target.controller;

import com.finpilot.security.UserPrincipal;
import com.finpilot.target.dto.TargetRequest;
import com.finpilot.target.dto.TargetResponse;
import com.finpilot.target.service.TargetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/targets")
@RequiredArgsConstructor
public class TargetController {

    private final TargetService targetService;

    @GetMapping
    public ResponseEntity<List<TargetResponse>> getTargets(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(targetService.getUserTargets(userPrincipal.getUser()));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<TargetResponse> getTargetByCategoryId(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(targetService.getTargetByCategoryId(userPrincipal.getUser(), categoryId));
    }

    @PostMapping
    public ResponseEntity<TargetResponse> createOrUpdateTarget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody TargetRequest request) {
        return ResponseEntity.ok(targetService.createOrUpdateTarget(userPrincipal.getUser(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTarget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        targetService.deleteTarget(userPrincipal.getUser(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/snooze")
    public ResponseEntity<TargetResponse> snoozeTarget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ResponseEntity.ok(targetService.snoozeTarget(userPrincipal.getUser(), id));
    }

    @PostMapping("/{id}/unsnooze")
    public ResponseEntity<TargetResponse> unsnoozeTarget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ResponseEntity.ok(targetService.unsnoozeTarget(userPrincipal.getUser(), id));
    }
}

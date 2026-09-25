package com.finpilot.ai.controller;

import com.finpilot.ai.dto.*;
import com.finpilot.ai.exception.AiErrorResponse;
import com.finpilot.ai.exception.AiException;
import com.finpilot.ai.service.AiService;
import com.finpilot.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;

    @GetMapping("/health")
    public ResponseEntity<AiHealthResponse> getHealth() {
        return ResponseEntity.ok(aiService.checkHealth());
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AiChatRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(aiService.chat(principal.getUser(), request.getMessage()));
    }

    @GetMapping("/insights/spending")
    public ResponseEntity<AiInsightsResponse> getSpendingInsights(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(aiService.generateSpendingInsights(principal.getUser(), startDate, endDate, month, year));
    }

    @GetMapping("/insights/budget")
    public ResponseEntity<AiInsightsResponse> getBudgetInsights(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(aiService.generateBudgetInsights(principal.getUser(), year, month));
    }

    @GetMapping("/insights/recurring")
    public ResponseEntity<AiInsightsResponse> getRecurringInsights(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(aiService.generateRecurringInsights(principal.getUser()));
    }

    @GetMapping("/summary/monthly")
    public ResponseEntity<AiInsightsResponse> getMonthlySummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(aiService.generateMonthlySummary(principal.getUser(), year, month));
    }

    @PostMapping("/suggest-category")
    public ResponseEntity<SuggestCategoryResponse> suggestCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody SuggestCategoryRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(aiService.suggestCategory(principal.getUser(), request));
    }

    @ExceptionHandler(AiException.class)
    public ResponseEntity<AiErrorResponse> handleAiException(AiException ex) {
        log.warn("Handled AI exception [{}]: {}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus())
                .body(AiErrorResponse.builder()
                        .code(ex.getCode())
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AiErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled error in AI controller: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AiErrorResponse.builder()
                        .code("AI_ERROR")
                        .message("An error occurred while communicating with local AI.")
                        .build());
    }
}

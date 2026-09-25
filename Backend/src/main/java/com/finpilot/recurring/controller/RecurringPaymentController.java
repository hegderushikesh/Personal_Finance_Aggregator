package com.finpilot.recurring.controller;

import com.finpilot.recurring.dto.DetectRecurringRequest;
import com.finpilot.recurring.dto.DetectRecurringResponse;
import com.finpilot.recurring.dto.RecurringPaymentResponse;
import com.finpilot.recurring.dto.UpdateRecurringPaymentRequest;
import com.finpilot.recurring.entity.RecurringFrequency;
import com.finpilot.recurring.entity.RecurringPaymentStatus;
import com.finpilot.recurring.service.RecurringPaymentService;
import com.finpilot.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-payments")
@RequiredArgsConstructor
public class RecurringPaymentController {

    private final RecurringPaymentService recurringPaymentService;

    @GetMapping
    public ResponseEntity<List<RecurringPaymentResponse>> getRecurringPayments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) RecurringPaymentStatus status,
            @RequestParam(required = false) RecurringFrequency frequency,
            @RequestParam(required = false) Boolean upcoming
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(recurringPaymentService.getRecurringPayments(
                principal.getUser(), status, frequency, upcoming
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringPaymentResponse> getRecurringPaymentById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(recurringPaymentService.getRecurringPaymentById(principal.getUser(), id));
    }

    @PostMapping("/detect")
    public ResponseEntity<DetectRecurringResponse> detectRecurringPayments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) DetectRecurringRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(recurringPaymentService.detectRecurringPayments(principal.getUser(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringPaymentResponse> updateRecurringPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecurringPaymentRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(recurringPaymentService.updateRecurringPayment(principal.getUser(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        recurringPaymentService.deleteRecurringPayment(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}

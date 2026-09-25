package com.finpilot.plaid.controller;

import com.finpilot.plaid.dto.*;
import com.finpilot.plaid.service.PlaidService;
import com.finpilot.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plaid")
@RequiredArgsConstructor
public class PlaidController {

    private final PlaidService plaidService;

    @PostMapping("/link-token")
    public ResponseEntity<LinkTokenResponse> createLinkToken(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(plaidService.createLinkToken(principal.getUser()));
    }

    @PostMapping("/exchange-token")
    public ResponseEntity<ExchangeTokenResponse> exchangePublicToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ExchangeTokenRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(plaidService.exchangePublicToken(principal.getUser(), request));
    }

    @GetMapping("/connections")
    public ResponseEntity<List<PlaidConnectionResponse>> getConnections(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(plaidService.getConnections(principal.getUser()));
    }

    @GetMapping("/connections/{id}")
    public ResponseEntity<PlaidConnectionResponse> getConnection(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(plaidService.getConnection(principal.getUser(), id));
    }

    @PostMapping("/sync/{connectionId}")
    public ResponseEntity<SyncResponse> syncConnection(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long connectionId
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(plaidService.syncConnection(principal.getUser(), connectionId));
    }

    @DeleteMapping("/connections/{id}")
    public ResponseEntity<Void> disconnect(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        plaidService.disconnect(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}

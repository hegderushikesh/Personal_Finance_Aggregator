package com.finpilot.budget.controller;

import com.finpilot.budget.dto.*;
import com.finpilot.budget.entity.Budget;
import com.finpilot.budget.repository.BudgetRepository;
import com.finpilot.budget.service.AutoAssignService;
import com.finpilot.budget.service.BudgetAllocationService;
import com.finpilot.budget.service.BudgetService;
import com.finpilot.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetAllocationService budgetAllocationService;
    private final AutoAssignService autoAssignService;
    private final BudgetRepository budgetRepository;

    @GetMapping("/api/budgets/current")
    public ResponseEntity<BudgetResponse> getCurrentBudget(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(budgetService.getOrCreateBudget(userPrincipal.getUser(), null, null));
    }

    @GetMapping("/api/budgets")
    public ResponseEntity<BudgetResponse> getBudget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(budgetService.getOrCreateBudget(userPrincipal.getUser(), year, month));
    }

    @PostMapping("/api/budgets/{id}/starting-balance")
    public ResponseEntity<BudgetResponse> setStartingBalance(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody StartingBalanceRequest request) {
        return ResponseEntity.ok(budgetService.setStartingBalance(userPrincipal.getUser(), id, request.getStartingBalance()));
    }

    @PostMapping("/api/budgets/{id}/assign")
    public ResponseEntity<BudgetResponse> assignMoney(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody AssignMoneyRequest request) {
        budgetAllocationService.assignMoney(userPrincipal.getUser(), id, request);
        Budget budget = budgetRepository.findByIdAndUser(id, userPrincipal.getUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
        return ResponseEntity.ok(budgetService.buildBudgetResponse(userPrincipal.getUser(), budget));
    }

    @PostMapping("/api/budgets/{id}/move-money")
    public ResponseEntity<BudgetResponse> moveMoney(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody MoveMoneyRequest request) {
        budgetAllocationService.moveMoney(userPrincipal.getUser(), id, request);
        Budget budget = budgetRepository.findByIdAndUser(id, userPrincipal.getUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
        return ResponseEntity.ok(budgetService.buildBudgetResponse(userPrincipal.getUser(), budget));
    }

    @PostMapping("/api/budgets/{id}/copy-previous-month")
    public ResponseEntity<BudgetResponse> copyPreviousMonth(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ResponseEntity.ok(budgetService.copyPreviousMonth(userPrincipal.getUser(), id));
    }

    @PostMapping("/api/budgets/{id}/auto-assign/preview")
    public ResponseEntity<AutoAssignPreviewResponse> autoAssignPreview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody AutoAssignRequest request) {
        Budget budget = budgetRepository.findByIdAndUser(id, userPrincipal.getUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
        return ResponseEntity.ok(autoAssignService.generatePreview(userPrincipal.getUser(), budget, request.getMode()));
    }

    @PostMapping("/api/budgets/{id}/auto-assign/apply")
    public ResponseEntity<BudgetResponse> autoAssignApply(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody AutoAssignRequest request) {
        Budget budget = budgetRepository.findByIdAndUser(id, userPrincipal.getUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
        autoAssignService.applyAutoAssign(userPrincipal.getUser(), budget, request.getMode());
        return ResponseEntity.ok(budgetService.buildBudgetResponse(userPrincipal.getUser(), budget));
    }

    @PostMapping("/api/budgets/{id}/undo")
    public ResponseEntity<BudgetResponse> undoLastActivity(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        return ResponseEntity.ok(budgetService.undoLastActivity(userPrincipal.getUser(), id));
    }

    @GetMapping("/api/categories/{id}/details")
    public ResponseEntity<CategoryDetailsResponse> getCategoryDetails(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(budgetService.getCategoryDetails(userPrincipal.getUser(), id, year, month));
    }
}

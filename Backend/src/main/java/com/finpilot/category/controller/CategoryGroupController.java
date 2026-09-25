package com.finpilot.category.controller;

import com.finpilot.category.dto.CategoryGroupRequest;
import com.finpilot.category.dto.CategoryGroupResponse;
import com.finpilot.category.service.CategoryGroupService;
import com.finpilot.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category-groups")
@RequiredArgsConstructor
public class CategoryGroupController {

    private final CategoryGroupService categoryGroupService;

    @GetMapping
    public ResponseEntity<List<CategoryGroupResponse>> getCategoryGroups(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(categoryGroupService.getUserCategoryGroups(userPrincipal.getUser()));
    }

    @PostMapping
    public ResponseEntity<CategoryGroupResponse> createCategoryGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CategoryGroupRequest request) {
        return ResponseEntity.ok(categoryGroupService.createCategoryGroup(userPrincipal.getUser(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryGroupResponse> updateCategoryGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody CategoryGroupRequest request) {
        return ResponseEntity.ok(categoryGroupService.updateCategoryGroup(userPrincipal.getUser(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        categoryGroupService.deleteCategoryGroup(userPrincipal.getUser(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/toggle-collapse")
    public ResponseEntity<Void> toggleCollapse(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        categoryGroupService.toggleCollapse(userPrincipal.getUser(), id);
        return ResponseEntity.ok().build();
    }
}

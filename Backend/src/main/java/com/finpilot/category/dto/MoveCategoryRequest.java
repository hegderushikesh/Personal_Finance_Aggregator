package com.finpilot.category.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveCategoryRequest {
    @NotNull(message = "Target category group ID is required")
    private Long targetGroupId;
}

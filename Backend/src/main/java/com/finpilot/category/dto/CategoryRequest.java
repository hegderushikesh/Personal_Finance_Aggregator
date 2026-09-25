package com.finpilot.category.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @NotNull(message = "Category group ID is required")
    private Long categoryGroupId;

    @NotBlank(message = "Category name is required")
    private String name;

    private String icon;
    private String color;
    private Integer sortOrder;
    private String note;
}

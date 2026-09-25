package com.finpilot.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryGroupRequest {
    @NotBlank(message = "Category group name is required")
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
}

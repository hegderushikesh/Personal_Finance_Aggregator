package com.finpilot.category.dto;

import com.finpilot.category.entity.Category;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private Long categoryGroupId;
    private String categoryGroupName;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Boolean isHidden;
    private String note;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .categoryGroupId(category.getCategoryGroup().getId())
                .categoryGroupName(category.getCategoryGroup().getName())
                .name(category.getName())
                .icon(category.getIcon())
                .color(category.getColor())
                .sortOrder(category.getSortOrder())
                .isHidden(category.getIsHidden())
                .note(category.getNote())
                .build();
    }
}

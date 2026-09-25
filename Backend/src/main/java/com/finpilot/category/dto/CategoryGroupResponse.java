package com.finpilot.category.dto;

import com.finpilot.category.entity.CategoryGroup;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryGroupResponse {
    private Long id;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Boolean isCollapsed;
    private List<CategoryResponse> categories;

    public static CategoryGroupResponse from(CategoryGroup group, List<CategoryResponse> categories) {
        return CategoryGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .icon(group.getIcon())
                .color(group.getColor())
                .sortOrder(group.getSortOrder())
                .isCollapsed(group.getIsCollapsed())
                .categories(categories)
                .build();
    }
}

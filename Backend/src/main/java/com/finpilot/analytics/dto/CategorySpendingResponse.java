package com.finpilot.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySpendingResponse {
    private Long categoryId;
    private String category;
    
    @JsonProperty("group")
    private String group;
    
    private String categoryGroup;
    private BigDecimal amount;
    private long transactionCount;

    public static class CategorySpendingResponseBuilder {
        public CategorySpendingResponseBuilder group(String group) {
            this.group = group;
            this.categoryGroup = group;
            return this;
        }

        public CategorySpendingResponseBuilder categoryGroup(String categoryGroup) {
            this.categoryGroup = categoryGroup;
            this.group = categoryGroup;
            return this;
        }
    }
}

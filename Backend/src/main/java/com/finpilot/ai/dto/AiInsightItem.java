package com.finpilot.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInsightItem {
    private String title;
    private String description;
    @Builder.Default
    private String severity = "INFO"; // INFO, NOTICE, WARNING
}

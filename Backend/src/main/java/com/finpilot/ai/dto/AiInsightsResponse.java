package com.finpilot.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInsightsResponse {
    private String summary;
    @Builder.Default
    private List<AiInsightItem> insights = new ArrayList<>();
    @Builder.Default
    private List<String> observations = new ArrayList<>();
}

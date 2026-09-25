package com.finpilot.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finpilot.ai.client.OllamaClient;
import com.finpilot.ai.dto.*;
import com.finpilot.ai.dto.ollama.OllamaChatMessage;
import com.finpilot.ai.dto.ollama.OllamaChatRequest;
import com.finpilot.ai.dto.ollama.OllamaChatResponse;
import com.finpilot.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final OllamaClient ollamaClient;
    private final FinanceContextService financeContextService;
    private final FinanceIntentRouter intentRouter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiHealthResponse checkHealth() {
        boolean available = ollamaClient.isModelAvailable();
        return AiHealthResponse.builder()
                .available(available)
                .model(ollamaClient.getModelName())
                .message(available ? null : "Ollama is unavailable")
                .build();
    }

    public AiChatResponse chat(User user, String userMessage) {
        log.info("Processing AI chat request for user ID: {}", user.getId());
        FinanceIntent intent = intentRouter.route(userMessage);
        log.info("Detected intent: {} for user query", intent);

        Map<String, Object> context = financeContextService.buildIntentContext(user, intent);
        String userPrompt = AiPromptTemplates.buildChatUserPrompt(userMessage, context);

        String reply = callModel(userPrompt);

        return AiChatResponse.builder()
                .message(reply)
                .insights(new ArrayList<>())
                .model(ollamaClient.getModelName())
                .build();
    }

    public AiInsightsResponse generateSpendingInsights(User user, LocalDate startDate, LocalDate endDate, Integer month, Integer year) {
        log.info("Generating spending insights for user ID: {}", user.getId());
        Map<String, Object> context = financeContextService.buildSpendingContext(user, startDate, endDate, month, year);
        String prompt = AiPromptTemplates.buildSpendingInsightsPrompt(context);
        String response = callModel(prompt);
        return parseInsightsResponse(response, "Spending Insights");
    }

    public AiInsightsResponse generateBudgetInsights(User user, Integer year, Integer month) {
        log.info("Generating budget insights for user ID: {}", user.getId());
        Map<String, Object> context = financeContextService.buildBudgetContext(user, year, month);
        String prompt = AiPromptTemplates.buildBudgetInsightsPrompt(context);
        String response = callModel(prompt);
        return parseInsightsResponse(response, "Budget Insights");
    }

    public AiInsightsResponse generateRecurringInsights(User user) {
        log.info("Generating recurring payment insights for user ID: {}", user.getId());
        Map<String, Object> context = financeContextService.buildRecurringContext(user);
        String prompt = AiPromptTemplates.buildRecurringInsightsPrompt(context);
        String response = callModel(prompt);
        return parseInsightsResponse(response, "Recurring Payments");
    }

    public AiInsightsResponse generateMonthlySummary(User user, Integer year, Integer month) {
        log.info("Generating monthly financial summary for user ID: {}", user.getId());
        Map<String, Object> context = financeContextService.buildMonthlySummaryContext(user, year, month);
        String prompt = AiPromptTemplates.buildMonthlySummaryPrompt(context);
        String response = callModel(prompt);
        return parseInsightsResponse(response, "Monthly Financial Summary");
    }

    public SuggestCategoryResponse suggestCategory(User user, SuggestCategoryRequest request) {
        log.info("Suggesting category for user ID: {} and merchant: {}", user.getId(), request.getMerchantName());
        List<String> categories = financeContextService.getUserCategoryNames(user);
        String prompt = AiPromptTemplates.buildSuggestCategoryPrompt(
                request.getMerchantName(),
                request.getDescription(),
                request.getAmount(),
                categories
        );

        String response = callModel(prompt);
        return parseCategorySuggestionResponse(response, categories);
    }

    private String callModel(String userPrompt) {
        List<OllamaChatMessage> messages = List.of(
                OllamaChatMessage.builder()
                        .role("system")
                        .content(AiPromptTemplates.SYSTEM_PROMPT)
                        .build(),
                OllamaChatMessage.builder()
                        .role("user")
                        .content(userPrompt)
                        .build()
        );

        OllamaChatRequest request = OllamaChatRequest.builder()
                .model(ollamaClient.getModelName())
                .messages(messages)
                .stream(false)
                .build();

        OllamaChatResponse chatResponse = ollamaClient.chat(request);
        if (chatResponse == null || chatResponse.getMessage() == null || chatResponse.getMessage().getContent() == null) {
            return "No response received from local AI model.";
        }
        return chatResponse.getMessage().getContent().trim();
    }

    private AiInsightsResponse parseInsightsResponse(String rawContent, String fallbackTitle) {
        String cleanJson = extractJson(rawContent);
        try {
            return objectMapper.readValue(cleanJson, AiInsightsResponse.class);
        } catch (Exception e) {
            log.warn("First JSON parse attempt failed for insights. Attempting repair: {}", e.getMessage());
            try {
                JsonNode root = objectMapper.readTree(cleanJson);
                String summary = root.path("summary").asText(rawContent);
                List<AiInsightItem> items = new ArrayList<>();
                if (root.has("insights") && root.get("insights").isArray()) {
                    for (JsonNode itemNode : root.get("insights")) {
                        items.add(AiInsightItem.builder()
                                .title(itemNode.path("title").asText(fallbackTitle))
                                .description(itemNode.path("description").asText(""))
                                .severity(itemNode.path("severity").asText("INFO"))
                                .build());
                    }
                }
                List<String> observations = new ArrayList<>();
                if (root.has("observations") && root.get("observations").isArray()) {
                    for (JsonNode obsNode : root.get("observations")) {
                        observations.add(obsNode.asText());
                    }
                }
                return AiInsightsResponse.builder()
                        .summary(summary)
                        .insights(items)
                        .observations(observations)
                        .build();
            } catch (Exception ex) {
                log.warn("Failed to parse AI response as JSON. Falling back to safe plain text representation: {}", ex.getMessage());
                return AiInsightsResponse.builder()
                        .summary(rawContent)
                        .insights(List.of(
                                AiInsightItem.builder()
                                        .title(fallbackTitle)
                                        .description(rawContent)
                                        .severity("INFO")
                                        .build()
                        ))
                        .observations(List.of())
                        .build();
            }
        }
    }

    private SuggestCategoryResponse parseCategorySuggestionResponse(String rawContent, List<String> availableCategories) {
        String cleanJson = extractJson(rawContent);
        try {
            return objectMapper.readValue(cleanJson, SuggestCategoryResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse category suggestion JSON: {}. Falling back to heuristic match.", e.getMessage());
            String defaultCategory = availableCategories.isEmpty() ? "Other" : availableCategories.get(0);
            return SuggestCategoryResponse.builder()
                    .suggestedCategory(defaultCategory)
                    .reason("Suggested based on available categories.")
                    .build();
        }
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        trimmed = trimmed.trim();

        int openBrace = trimmed.indexOf('{');
        int closeBrace = trimmed.lastIndexOf('}');
        if (openBrace != -1 && closeBrace != -1 && closeBrace > openBrace) {
            return trimmed.substring(openBrace, closeBrace + 1);
        }
        return trimmed;
    }
}

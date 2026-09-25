package com.finpilot.ai.service;

import com.finpilot.ai.client.OllamaClient;
import com.finpilot.ai.dto.*;
import com.finpilot.ai.dto.ollama.OllamaChatMessage;
import com.finpilot.ai.dto.ollama.OllamaChatRequest;
import com.finpilot.ai.dto.ollama.OllamaChatResponse;
import com.finpilot.ai.exception.AiException;
import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.Role;
import com.finpilot.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private OllamaClient ollamaClient;
    @Mock
    private FinanceContextService financeContextService;
    @Mock
    private FinanceIntentRouter intentRouter;

    @InjectMocks
    private AiService aiService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@finpilot.com")
                .name("FinPilot Tester")
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .build();
    }

    @Test
    @DisplayName("Health check returns available status and model name")
    void testCheckHealth() {
        when(ollamaClient.isModelAvailable()).thenReturn(true);
        when(ollamaClient.getModelName()).thenReturn("qwen2.5-coder:7b");

        AiHealthResponse health = aiService.checkHealth();
        assertTrue(health.isAvailable());
        assertEquals("qwen2.5-coder:7b", health.getModel());
    }

    @Test
    @DisplayName("Chat handles query and returns model response")
    void testChatSuccess() {
        when(intentRouter.route(any())).thenReturn(FinanceIntent.SPENDING);
        when(financeContextService.buildIntentContext(any(), any()))
                .thenReturn(Map.of("topCategory", "Bills", "totalSpending", new BigDecimal("9000.00")));
        when(ollamaClient.getModelName()).thenReturn("qwen2.5-coder:7b");
        when(ollamaClient.chat(any(OllamaChatRequest.class))).thenReturn(
                OllamaChatResponse.builder()
                        .model("qwen2.5-coder:7b")
                        .message(OllamaChatMessage.builder().role("assistant").content("Your largest expense was Bills.").build())
                        .build()
        );

        AiChatResponse response = aiService.chat(testUser, "Where did I spend the most?");
        assertNotNull(response);
        assertEquals("Your largest expense was Bills.", response.getMessage());
        assertEquals("qwen2.5-coder:7b", response.getModel());
    }

    @Test
    @DisplayName("Spending insights parses valid JSON structure")
    void testSpendingInsightsJsonParsing() {
        when(financeContextService.buildSpendingContext(any(), any(), any(), any(), any()))
                .thenReturn(Map.of("period", "September 2026", "totalSpending", new BigDecimal("15000.00")));
        when(ollamaClient.getModelName()).thenReturn("qwen2.5-coder:7b");

        String jsonResponse = """
                ```json
                {
                  "summary": "You spent 15000 in September.",
                  "insights": [
                    {
                      "title": "Bills concentration",
                      "description": "Bills accounted for 60% of expenses.",
                      "severity": "INFO"
                    }
                  ],
                  "observations": [
                    "High utility costs"
                  ]
                }
                ```
                """;

        when(ollamaClient.chat(any())).thenReturn(
                OllamaChatResponse.builder()
                        .message(OllamaChatMessage.builder().role("assistant").content(jsonResponse).build())
                        .build()
        );

        AiInsightsResponse insights = aiService.generateSpendingInsights(testUser, null, null, 9, 2026);
        assertNotNull(insights);
        assertEquals("You spent 15000 in September.", insights.getSummary());
        assertEquals(1, insights.getInsights().size());
        assertEquals("Bills concentration", insights.getInsights().get(0).getTitle());
    }

    @Test
    @DisplayName("Invalid JSON response falls back gracefully without throwing exception")
    void testInvalidJsonFallback() {
        when(financeContextService.buildSpendingContext(any(), any(), any(), any(), any()))
                .thenReturn(Map.of());
        when(ollamaClient.getModelName()).thenReturn("qwen2.5-coder:7b");
        when(ollamaClient.chat(any())).thenReturn(
                OllamaChatResponse.builder()
                        .message(OllamaChatMessage.builder().role("assistant").content("This is plain text without valid JSON formatting.").build())
                        .build()
        );

        AiInsightsResponse response = aiService.generateSpendingInsights(testUser, null, null, 9, 2026);
        assertNotNull(response);
        assertNotNull(response.getSummary());
        assertTrue(response.getSummary().contains("This is plain text"));
    }

    @Test
    @DisplayName("Suggest category successfully identifies suggested category")
    void testSuggestCategory() {
        when(financeContextService.getUserCategoryNames(testUser)).thenReturn(List.of("Food", "Transport", "Bills"));
        when(ollamaClient.getModelName()).thenReturn("qwen2.5-coder:7b");
        when(ollamaClient.chat(any())).thenReturn(
                OllamaChatResponse.builder()
                        .message(OllamaChatMessage.builder().role("assistant").content("""
                                {
                                  "suggestedCategory": "Food",
                                  "reason": "Swiggy is typically food delivery."
                                }
                                """).build())
                        .build()
        );

        SuggestCategoryResponse response = aiService.suggestCategory(testUser, SuggestCategoryRequest.builder()
                .merchantName("Swiggy")
                .description("Food delivery")
                .amount(new BigDecimal("450.00"))
                .build());

        assertNotNull(response);
        assertEquals("Food", response.getSuggestedCategory());
        assertEquals("Swiggy is typically food delivery.", response.getReason());
    }

    @Test
    @DisplayName("Ollama offline propagates AiException with AI_UNAVAILABLE code")
    void testOllamaUnavailable() {
        when(intentRouter.route(any())).thenReturn(FinanceIntent.SPENDING);
        when(financeContextService.buildIntentContext(any(), any())).thenReturn(Map.of());
        when(ollamaClient.getModelName()).thenReturn("qwen2.5-coder:7b");
        when(ollamaClient.chat(any())).thenThrow(AiException.aiUnavailable());

        AiException ex = assertThrows(AiException.class, () -> aiService.chat(testUser, "Where did I spend?"));
        assertEquals("AI_UNAVAILABLE", ex.getCode());
    }

    @Test
    @DisplayName("Ollama timeout propagates AiException with AI_TIMEOUT code")
    void testOllamaTimeout() {
        when(intentRouter.route(any())).thenReturn(FinanceIntent.SPENDING);
        when(financeContextService.buildIntentContext(any(), any())).thenReturn(Map.of());
        when(ollamaClient.getModelName()).thenReturn("qwen2.5-coder:7b");
        when(ollamaClient.chat(any())).thenThrow(AiException.timeout());

        AiException ex = assertThrows(AiException.class, () -> aiService.chat(testUser, "Where did I spend?"));
        assertEquals("AI_TIMEOUT", ex.getCode());
        assertEquals(org.springframework.http.HttpStatus.GATEWAY_TIMEOUT, ex.getHttpStatus());
    }

    @Test
    @DisplayName("Model not found propagates AiException with MODEL_UNAVAILABLE code")
    void testModelUnavailable() {
        when(intentRouter.route(any())).thenReturn(FinanceIntent.SPENDING);
        when(financeContextService.buildIntentContext(any(), any())).thenReturn(Map.of());
        when(ollamaClient.getModelName()).thenReturn("qwen2.5-coder:7b");
        when(ollamaClient.chat(any())).thenThrow(AiException.modelUnavailable("qwen2.5-coder:7b"));

        AiException ex = assertThrows(AiException.class, () -> aiService.chat(testUser, "Where did I spend?"));
        assertEquals("MODEL_UNAVAILABLE", ex.getCode());
    }
}

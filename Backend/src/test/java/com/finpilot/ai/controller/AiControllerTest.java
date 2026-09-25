package com.finpilot.ai.controller;

import com.finpilot.ai.dto.*;
import com.finpilot.ai.exception.AiErrorResponse;
import com.finpilot.ai.exception.AiException;
import com.finpilot.ai.service.AiService;
import com.finpilot.security.UserPrincipal;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock
    private AiService aiService;

    @InjectMocks
    private AiController aiController;

    private User testUser;
    private UserPrincipal testUserPrincipal;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@finpilot.com")
                .name("FinPilot User")
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .build();

        testUserPrincipal = UserPrincipal.create(testUser);
    }

    @Test
    @DisplayName("GET /api/ai/health returns 200 OK")
    void testGetHealth() {
        when(aiService.checkHealth()).thenReturn(
                AiHealthResponse.builder().available(true).model("qwen2.5-coder:7b").build()
        );

        ResponseEntity<AiHealthResponse> response = aiController.getHealth();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("qwen2.5-coder:7b", response.getBody().getModel());
    }

    @Test
    @DisplayName("POST /api/ai/chat returns 200 OK when authenticated")
    void testChat_Authenticated() {
        AiChatRequest request = AiChatRequest.builder().message("Where did I spend?").build();
        when(aiService.chat(eq(testUser), eq("Where did I spend?")))
                .thenReturn(AiChatResponse.builder().message("Your highest category was Bills.").model("qwen2.5-coder:7b").build());

        ResponseEntity<AiChatResponse> response = aiController.chat(testUserPrincipal, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Your highest category was Bills.", response.getBody().getMessage());
    }

    @Test
    @DisplayName("POST /api/ai/chat returns 401 UNAUTHORIZED when unauthenticated")
    void testChat_Unauthenticated() {
        AiChatRequest request = AiChatRequest.builder().message("Where did I spend?").build();
        ResponseEntity<AiChatResponse> response = aiController.chat(null, request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /api/ai/insights/spending returns 200 OK")
    void testGetSpendingInsights() {
        when(aiService.generateSpendingInsights(eq(testUser), any(), any(), eq(9), eq(2026)))
                .thenReturn(AiInsightsResponse.builder().summary("Spending summary").insights(List.of()).build());

        ResponseEntity<AiInsightsResponse> response = aiController.getSpendingInsights(testUserPrincipal, null, null, 9, 2026);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Spending summary", response.getBody().getSummary());
    }

    @Test
    @DisplayName("GET /api/ai/insights/budget returns 200 OK")
    void testGetBudgetInsights() {
        when(aiService.generateBudgetInsights(eq(testUser), eq(2026), eq(9)))
                .thenReturn(AiInsightsResponse.builder().summary("Budget summary").insights(List.of()).build());

        ResponseEntity<AiInsightsResponse> response = aiController.getBudgetInsights(testUserPrincipal, 2026, 9);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Budget summary", response.getBody().getSummary());
    }

    @Test
    @DisplayName("GET /api/ai/insights/recurring returns 200 OK")
    void testGetRecurringInsights() {
        when(aiService.generateRecurringInsights(eq(testUser)))
                .thenReturn(AiInsightsResponse.builder().summary("Recurring summary").insights(List.of()).build());

        ResponseEntity<AiInsightsResponse> response = aiController.getRecurringInsights(testUserPrincipal);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Recurring summary", response.getBody().getSummary());
    }

    @Test
    @DisplayName("GET /api/ai/summary/monthly returns 200 OK")
    void testGetMonthlySummary() {
        when(aiService.generateMonthlySummary(eq(testUser), eq(2026), eq(9)))
                .thenReturn(AiInsightsResponse.builder().summary("Monthly summary").insights(List.of()).build());

        ResponseEntity<AiInsightsResponse> response = aiController.getMonthlySummary(testUserPrincipal, 2026, 9);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Monthly summary", response.getBody().getSummary());
    }

    @Test
    @DisplayName("POST /api/ai/suggest-category returns 200 OK")
    void testSuggestCategory() {
        SuggestCategoryRequest req = SuggestCategoryRequest.builder()
                .merchantName("Swiggy")
                .description("Food delivery")
                .amount(new BigDecimal("450.00"))
                .build();

        when(aiService.suggestCategory(eq(testUser), eq(req)))
                .thenReturn(SuggestCategoryResponse.builder().suggestedCategory("Food").reason("Food delivery").build());

        ResponseEntity<SuggestCategoryResponse> response = aiController.suggestCategory(testUserPrincipal, req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Food", response.getBody().getSuggestedCategory());
    }

    @Test
    @DisplayName("AiException handler produces structured JSON without 500 stack trace")
    void testHandleAiException() {
        AiException ex = AiException.aiUnavailable();
        ResponseEntity<AiErrorResponse> response = aiController.handleAiException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AI_UNAVAILABLE", response.getBody().getCode());
        assertEquals("Local AI is currently unavailable. Please make sure Ollama is running.", response.getBody().getMessage());
    }
}

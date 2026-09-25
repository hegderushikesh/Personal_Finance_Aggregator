package com.finpilot.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finpilot.ai.config.OllamaProperties;
import com.finpilot.ai.dto.ollama.OllamaChatMessage;
import com.finpilot.ai.dto.ollama.OllamaChatRequest;
import com.finpilot.ai.dto.ollama.OllamaChatResponse;
import com.finpilot.ai.dto.ollama.OllamaTagsResponse;
import com.finpilot.ai.exception.AiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OllamaClientTest {

    private OllamaProperties properties;
    private MockRestServiceServer mockServer;
    private OllamaClientImpl ollamaClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        properties = new OllamaProperties();
        properties.setBaseUrl("http://localhost:11434");
        properties.setModel("qwen2.5-coder:7b");
        properties.setTimeoutSeconds(60);

        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
        mockServer = MockRestServiceServer.bindTo(builder).build();
        ollamaClient = new OllamaClientImpl(properties, builder.build());
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("getModelName returns configured model")
    void testGetModelName() {
        assertEquals("qwen2.5-coder:7b", ollamaClient.getModelName());
    }

    @Test
    @DisplayName("isModelAvailable returns true when target model is present in /api/tags")
    void testIsModelAvailableSuccess() throws Exception {
        OllamaTagsResponse response = OllamaTagsResponse.builder()
                .models(List.of(
                        OllamaTagsResponse.ModelInfo.builder().name("qwen2.5-coder:7b").model("qwen2.5-coder:7b").build()
                ))
                .build();

        mockServer.expect(requestTo("http://localhost:11434/api/tags"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        boolean available = ollamaClient.isModelAvailable();

        assertTrue(available);
        mockServer.verify();
    }

    @Test
    @DisplayName("isModelAvailable returns false when target model is not in /api/tags")
    void testIsModelAvailableModelMissing() throws Exception {
        OllamaTagsResponse response = OllamaTagsResponse.builder()
                .models(List.of(
                        OllamaTagsResponse.ModelInfo.builder().name("llama3:8b").model("llama3:8b").build()
                ))
                .build();

        mockServer.expect(requestTo("http://localhost:11434/api/tags"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        boolean available = ollamaClient.isModelAvailable();

        assertFalse(available);
        mockServer.verify();
    }

    @Test
    @DisplayName("isModelAvailable returns false when Ollama service is unreachable")
    void testIsModelAvailableServiceUnreachable() {
        mockServer.expect(requestTo("http://localhost:11434/api/tags"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        boolean available = ollamaClient.isModelAvailable();

        assertFalse(available);
        mockServer.verify();
    }

    @Test
    @DisplayName("chat returns response successfully")
    void testChatSuccess() throws Exception {
        OllamaChatRequest request = OllamaChatRequest.builder()
                .model("qwen2.5-coder:7b")
                .messages(List.of(OllamaChatMessage.builder().role("user").content("Hello").build()))
                .stream(false)
                .build();

        OllamaChatResponse response = OllamaChatResponse.builder()
                .model("qwen2.5-coder:7b")
                .message(OllamaChatMessage.builder().role("assistant").content("Hello! How can I assist you with your finances?").build())
                .done(true)
                .build();

        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        OllamaChatResponse result = ollamaClient.chat(request);

        assertNotNull(result);
        assertNotNull(result.getMessage());
        assertEquals("Hello! How can I assist you with your finances?", result.getMessage().getContent());
        mockServer.verify();
    }

    @Test
    @DisplayName("chat throws modelUnavailable when Ollama returns 404")
    void testChatModelNotFound() {
        OllamaChatRequest request = OllamaChatRequest.builder()
                .model("qwen2.5-coder:7b")
                .messages(List.of(OllamaChatMessage.builder().role("user").content("Hello").build()))
                .build();

        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        AiException ex = assertThrows(AiException.class, () -> ollamaClient.chat(request));
        assertEquals("MODEL_UNAVAILABLE", ex.getCode());
        mockServer.verify();
    }

    @Test
    @DisplayName("chat throws AI_ERROR when Ollama returns 500 error")
    void testChatServerError() {
        OllamaChatRequest request = OllamaChatRequest.builder()
                .model("qwen2.5-coder:7b")
                .messages(List.of(OllamaChatMessage.builder().role("user").content("Hello").build()))
                .build();

        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        AiException ex = assertThrows(AiException.class, () -> ollamaClient.chat(request));
        assertEquals("OLLAMA_ERROR", ex.getCode());
        mockServer.verify();
    }
}

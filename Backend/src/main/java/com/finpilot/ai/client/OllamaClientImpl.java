package com.finpilot.ai.client;

import com.finpilot.ai.config.OllamaProperties;
import com.finpilot.ai.dto.ollama.OllamaChatRequest;
import com.finpilot.ai.dto.ollama.OllamaChatResponse;
import com.finpilot.ai.dto.ollama.OllamaTagsResponse;
import com.finpilot.ai.exception.AiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Duration;

@Component
@Slf4j
public class OllamaClientImpl implements OllamaClient {

    private final OllamaProperties properties;
    private final RestClient restClient;

    @Autowired
    public OllamaClientImpl(OllamaProperties properties) {
        this(properties, createDefaultRestClient(properties));
    }

    public OllamaClientImpl(OllamaProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    private static RestClient createDefaultRestClient(OllamaProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(Math.max(10, properties.getTimeoutSeconds())));

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @Override
    public String getModelName() {
        return properties.getModel();
    }

    @Override
    public boolean isModelAvailable() {
        try {
            OllamaTagsResponse tags = restClient.get()
                    .uri("/api/tags")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(OllamaTagsResponse.class);

            if (tags == null || tags.getModels() == null) {
                return false;
            }

            String targetModel = properties.getModel() != null ? properties.getModel().trim() : "";
            return tags.getModels().stream()
                    .anyMatch(m -> matchesModel(m.getName(), targetModel) || matchesModel(m.getModel(), targetModel));
        } catch (Exception e) {
            log.warn("Ollama availability check failed at {}: {}", properties.getBaseUrl(), e.getMessage());
            return false;
        }
    }

    private boolean matchesModel(String availableModel, String targetModel) {
        if (availableModel == null || targetModel.isEmpty()) {
            return false;
        }
        if (availableModel.equalsIgnoreCase(targetModel)) {
            return true;
        }
        if (availableModel.endsWith("/" + targetModel)) {
            return true;
        }
        if (!targetModel.contains(":") && availableModel.startsWith(targetModel + ":")) {
            return true;
        }
        return false;
    }

    @Override
    public OllamaChatResponse chat(OllamaChatRequest request) {
        try {
            return restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaChatResponse.class);
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("Ollama chat request timed out after {}s", properties.getTimeoutSeconds());
                throw AiException.timeout();
            }
            log.error("Ollama connection failed: {}", e.getMessage());
            throw AiException.aiUnavailable();
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Ollama model not found: {}", properties.getModel());
            throw AiException.modelUnavailable(properties.getModel());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Ollama HTTP error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AiException("OLLAMA_ERROR", "Error communicating with local AI: " + e.getMessage(), org.springframework.http.HttpStatus.BAD_GATEWAY, e);
        } catch (Exception e) {
            log.error("Unexpected error contacting Ollama: {}", e.getMessage(), e);
            throw AiException.aiUnavailable();
        }
    }
}

package com.finpilot.ai.client;

import com.finpilot.ai.dto.ollama.OllamaChatRequest;
import com.finpilot.ai.dto.ollama.OllamaChatResponse;

public interface OllamaClient {
    OllamaChatResponse chat(OllamaChatRequest request);
    boolean isModelAvailable();
    String getModelName();
}

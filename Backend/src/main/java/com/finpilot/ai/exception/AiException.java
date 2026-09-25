package com.finpilot.ai.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AiException extends RuntimeException {

    private final String code;
    private final HttpStatus httpStatus;

    public AiException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public AiException(String code, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public static AiException aiUnavailable() {
        return new AiException(
                "AI_UNAVAILABLE",
                "Local AI is currently unavailable. Please make sure Ollama is running.",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    public static AiException modelUnavailable(String model) {
        return new AiException(
                "MODEL_UNAVAILABLE",
                "The local Qwen model is not available (" + model + ").",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    public static AiException timeout() {
        return new AiException(
                "AI_TIMEOUT",
                "AI response timed out. Please try again.",
                HttpStatus.GATEWAY_TIMEOUT
        );
    }
}

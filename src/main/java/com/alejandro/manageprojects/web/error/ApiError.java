package com.alejandro.manageprojects.web.error;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        LocalDateTime timestamp,
        String path,
        int status,
        ErrorType type,
        String code,
        String message,
        Map<String, Object> details
) {
    public static ApiError of(String path, int status, ErrorType type, String code, String message, Map<String, Object> details) {
        return new ApiError(LocalDateTime.now(), path, status, type, code, message, details);
    }
}

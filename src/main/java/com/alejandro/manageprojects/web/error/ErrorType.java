package com.alejandro.manageprojects.web.error;

public enum ErrorType {
    NOT_FOUND,
    VALIDATION,
    BAD_REQUEST,
    CONFLICT,
    SERVICE_UNAVAILABLE,
    INTERNAL,
    REDIS_UNAVAILABLE,
    DATA_INTEGRITY,
    TIMEOUT,
    EXTERNAL_SERVICE
}

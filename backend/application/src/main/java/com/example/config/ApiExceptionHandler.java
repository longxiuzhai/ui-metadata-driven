package com.example.config;

import com.example.auth.AuthException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> authentication(AuthException exception) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> duplicate(DuplicateKeyException exception) {
        return Map.of("message", "数据编码或名称已存在");
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(IllegalStateException exception) {
        return Map.of("message", exception.getMessage());
    }
}

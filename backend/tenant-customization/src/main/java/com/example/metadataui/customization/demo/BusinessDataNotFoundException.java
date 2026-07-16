package com.example.metadataui.customization.demo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BusinessDataNotFoundException extends RuntimeException {
    public BusinessDataNotFoundException(String message) { super(message); }
}

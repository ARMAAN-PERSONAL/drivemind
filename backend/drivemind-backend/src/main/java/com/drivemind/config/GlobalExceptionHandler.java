package com.drivemind.config;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Illegal arguments (most service errors)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        Map<String,Object> error = new HashMap<>();

        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(error);
    }


    // Login authentication errors
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String,Object>> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ){

        Map<String,Object> error = new HashMap<>();

        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.UNAUTHORIZED.value());
        error.put("error", "Unauthorized");
        error.put("message", "Invalid email or password");
        error.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }


    // DTO validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ){

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + " : " + err.getDefaultMessage())
                .orElse("Validation failed");

        Map<String,Object> error = new HashMap<>();

        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Validation Error");
        error.put("message", message);
        error.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(error);
    }


    // Catch other runtime errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {

        Map<String,Object> error = new HashMap<>();

        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Runtime Error");
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(error);
    }


    // Last fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception ex,
            HttpServletRequest request
    ) {

        Map<String,Object> error = new HashMap<>();

        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Internal Server Error");
        error.put("message", "Something went wrong on the server");
        error.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
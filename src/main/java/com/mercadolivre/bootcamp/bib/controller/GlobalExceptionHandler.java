package com.mercadolivre.bootcamp.bib.controller;

import com.mercadolivre.bootcamp.bib.controller.dto.response.ErrorResponseDTO;
import com.mercadolivre.bootcamp.bib.exception.BusinessRuleException;
import com.mercadolivre.bootcamp.bib.exception.ConflictException;
import com.mercadolivre.bootcamp.bib.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid JSON format or enum value",
                List.of("The request body is malformed or contains an invalid value."), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", List.of(ex.getMessage()), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflict(
            ConflictException ex,
            HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", List.of(ex.getMessage()), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessRule(
            BusinessRuleException ex,
            HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Business Rule Violation", List.of(ex.getMessage()), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                List.of("An unexpected error occurred. Please try again later."), request);
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(
            HttpStatus status,
            String error,
            List<String> messages,
            HttpServletRequest request) {

        ErrorResponseDTO response = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                error,
                messages,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(response);
    }
}

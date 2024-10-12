package com.kshrd.kroya_api.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", statusCode.value());
        body.put("error", ex.getFieldError().getDefaultMessage());
        return new ResponseEntity<>(body, statusCode);
    }

    @ExceptionHandler(FieldEmptyExceptionHandler.class)
    ProblemDetail handleFieldEmptyException(FieldEmptyExceptionHandler exceptionHandler) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exceptionHandler.getMessage());
        problemDetail.setTitle("Field Is Empty Exception");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setType(URI.create("localhost:8080/errors/bad-request"));
        return problemDetail;
    }

    @ExceptionHandler(NotFoundExceptionHandler.class)
    ProblemDetail handleNotFoundException(NotFoundExceptionHandler exceptionHandler) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exceptionHandler.getMessage());
        problemDetail.setTitle("Not Found Exception");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setType(URI.create("localhost:8080/errors/not-found"));
        return problemDetail;
    }

    @ExceptionHandler(DuplicateFieldExceptionHandler.class)
    ProblemDetail handleDuplicationException(DuplicateFieldExceptionHandler exceptionHandler) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exceptionHandler.getMessage());
        problemDetail.setTitle("Field Duplicate Exception");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setType(URI.create("localhost:8080/errors/field-duplication"));
        return problemDetail;
    }

    @ExceptionHandler(UserDuplicateExceptionHandler.class)
    ProblemDetail handleUerDuplicationException(UserDuplicateExceptionHandler exceptionHandler) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exceptionHandler.getMessage());
        problemDetail.setTitle("User Duplicate Exception");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setType(URI.create("localhost:8080/errors/user-duplication"));
        return problemDetail;
    }


    @ExceptionHandler(InvalidValueExceptionHandler.class)
    ProblemDetail handleInvalidException(InvalidValueExceptionHandler exceptionHandler) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exceptionHandler.getMessage());
        problemDetail.setTitle("Invalid Exception");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setType(URI.create("localhost:8080/errors/invalid-value"));
        return problemDetail;
    }

    // Handle exception BadRequest
    @ExceptionHandler(BadRequestException.class)
    ProblemDetail handleBadRequest(BadRequestException badRequestException) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, badRequestException.getMessage());
        return problemDetail;
    }
}
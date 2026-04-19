package com.sporty.feed.infrastructure.web.advice;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.sporty.feed.domain.model.UnknownOutcomeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Catches unknown {@code msg_type} / {@code type} discriminator values.
     * Jackson wraps {@code InvalidTypeIdException} inside {@link HttpMessageNotReadableException}.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        log.warn("Unreadable feed message: {}", ex.getMessage());
        if (ex.getCause() instanceof InvalidTypeIdException ite) {
            var subTypes = ite.getBaseType().getRawClass()
                    .getAnnotation(JsonSubTypes.class);
            var validNames = subTypes != null
                    ? java.util.Arrays.stream(subTypes.value())
                            .map(com.fasterxml.jackson.annotation.JsonSubTypes.Type::name)
                            .toList()
                    : List.of();
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Unknown type '" + ite.getTypeId() + "'. Valid values: " + validNames));
        }
        if (ex.getCause() instanceof UnrecognizedPropertyException upe) {
            String msg = "Unknown field '" + upe.getPropertyName() + "' is not allowed";
            return ResponseEntity.badRequest().body(new ErrorResponse(msg));
        }
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Unrecognized or malformed message format"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest()
                .body(new ValidationErrorResponse("Validation failed", errors));
    }

    @ExceptionHandler(UnknownOutcomeException.class)
    public ResponseEntity<ErrorResponse> handleUnknownOutcome(UnknownOutcomeException ex) {
        log.warn("Invalid outcome value: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getMessage()));
    }

    record ErrorResponse(String message) {}

    record ValidationErrorResponse(String message, List<String> errors) {}
}

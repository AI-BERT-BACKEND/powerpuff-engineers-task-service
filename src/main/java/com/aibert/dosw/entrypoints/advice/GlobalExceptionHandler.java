package com.aibert.dosw.entrypoints.advice;

import com.aibert.dosw.domain.exceptions.ExternalServiceUnavailableException;
import com.aibert.dosw.domain.exceptions.SubjectNotFoundException;
import com.aibert.dosw.domain.exceptions.SubjectNotInActiveSemesterException;
import com.aibert.dosw.domain.exceptions.TaskConflictException;
import com.aibert.dosw.domain.exceptions.TaskEditNotAllowedException;
import com.aibert.dosw.domain.exceptions.TaskForbiddenException;
import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized exception handler for all REST controllers.
 * Maps domain and validation exceptions to appropriate HTTP response codes and error bodies.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles bean validation failures triggered by {@code @Valid} annotations.
     * Returns a map of field names to their corresponding validation error messages.
     *
     * @param ex the validation exception containing one or more field errors
     * @return HTTP 400 with a map of {@code fieldName -> errorMessage}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });
        log.warn("VALIDATION_ERROR | fields={} | errorCount={}", errors.keySet(), errors.size());
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles cases where the referenced academic subject was not found.
     *
     * @param ex the exception thrown by the use case layer
     * @return HTTP 404 with a {@code message} body
     */
    @ExceptionHandler(SubjectNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSubjectNotFoundException(
            SubjectNotFoundException ex) {
        log.warn("NOT_FOUND | exception=SubjectNotFoundException | message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Handles cases where the subject exists but does not belong to the student's active semester (AIB-18.1 FA-03).
     *
     * @param ex the exception thrown by the use case layer
     * @return HTTP 422 with a {@code message} body
     */
    @ExceptionHandler(SubjectNotInActiveSemesterException.class)
    public ResponseEntity<Map<String, String>> handleSubjectNotInActiveSemesterException(
            SubjectNotInActiveSemesterException ex) {
        log.warn("UNPROCESSABLE | exception=SubjectNotInActiveSemesterException | message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Handles uniqueness constraint violations when creating a task.
     *
     * @param ex the exception thrown when a duplicate task is detected
     * @return HTTP 409 with a {@code message} body
     */
    @ExceptionHandler(TaskConflictException.class)
    public ResponseEntity<Map<String, String>> handleTaskConflictException(
            TaskConflictException ex) {
        log.warn("CONFLICT | exception=TaskConflictException | message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Handles lookup failures when a task cannot be found by its ID.
     *
     * @param ex the exception thrown when no task matches the given ID
     * @return HTTP 404 with a {@code message} body
     */
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTaskNotFoundException(
            TaskNotFoundException ex) {
        log.warn("NOT_FOUND | exception=TaskNotFoundException | message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Handles authorization failures when a student attempts to modify a task they do not own.
     *
     * @param ex the exception thrown by the use case layer
     * @return HTTP 403 with a {@code message} body
     */
    @ExceptionHandler(TaskForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleTaskForbiddenException(
            TaskForbiddenException ex) {
        log.warn("FORBIDDEN | exception=TaskForbiddenException | message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Handles attempts to edit a task that is already marked as COMPLETED.
     *
     * @param ex the exception thrown by the use case layer
     * @return HTTP 400 with a {@code message} body
     */
    @ExceptionHandler(TaskEditNotAllowedException.class)
    public ResponseEntity<Map<String, String>> handleTaskEditNotAllowedException(
            TaskEditNotAllowedException ex) {
        log.warn("EDIT_NOT_ALLOWED | exception=TaskEditNotAllowedException | message={}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Handles cases where an external service (e.g., academic-service) is unavailable
     * due to circuit breaker activation or fallback triggering.
     *
     * @param ex the exception thrown when a fallback or unavailability is detected
     * @return HTTP 503 with a {@code message} body
     */
    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleExternalServiceUnavailableException(
            ExternalServiceUnavailableException ex) {
        log.error("SERVICE_UNAVAILABLE | exception=ExternalServiceUnavailableException | message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Handles communication errors with external services (e.g., academic-service unavailable).
     *
     * @param ex the Feign client exception
     * @return HTTP 503 with a {@code message} body
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> handleFeignException(FeignException ex) {
        log.error("FEIGN_ERROR | exception=FeignException | status={} | message={}", ex.status(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "El servicio externo no está disponible. Inténtelo más tarde."));
    }

    /**
     * Catch-all handler for any unexpected exception not covered by a specific handler.
     *
     * @param ex the unexpected exception
     * @return HTTP 500 with a generic {@code message} body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("UNEXPECTED_ERROR | exception={} | message={}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Ha ocurrido un error inesperado. Inténtelo más tarde."));
    }
}

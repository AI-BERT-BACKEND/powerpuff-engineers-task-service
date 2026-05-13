package com.aibert.dosw.entrypoints.advice;

import com.aibert.dosw.domain.exceptions.SubjectNotFoundException;
import com.aibert.dosw.domain.exceptions.TaskConflictException;
import com.aibert.dosw.domain.exceptions.TaskEditNotAllowedException;
import com.aibert.dosw.domain.exceptions.TaskForbiddenException;
import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationExceptions_ShouldReturnBadRequestWithFieldErrors() {
        FieldError fieldError = new FieldError("task", "title", "El título es requerido");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("El título es requerido", response.getBody().get("title"));
    }

    @Test
    void handleSubjectNotFoundException_ShouldReturnNotFound() {
        SubjectNotFoundException ex = new SubjectNotFoundException("Subject MATH-101 not found");

        ResponseEntity<Map<String, String>> response = handler.handleSubjectNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Subject MATH-101 not found", response.getBody().get("message"));
    }

    @Test
    void handleTaskConflictException_ShouldReturnConflict() {
        TaskConflictException ex = new TaskConflictException("Task already exists");

        ResponseEntity<Map<String, String>> response = handler.handleTaskConflictException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Task already exists", response.getBody().get("message"));
    }

    @Test
    void handleTaskNotFoundException_ShouldReturnNotFound() {
        TaskNotFoundException ex = new TaskNotFoundException("task-id-123");

        ResponseEntity<Map<String, String>> response = handler.handleTaskNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tarea no encontrada con ID: task-id-123", response.getBody().get("message"));
    }

    @Test
    void handleTaskForbiddenException_ShouldReturnForbidden() {
        TaskForbiddenException ex = new TaskForbiddenException("task-id-99");

        ResponseEntity<Map<String, String>> response = handler.handleTaskForbiddenException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("task-id-99"));
    }

    @Test
    void handleTaskEditNotAllowedException_ShouldReturnBadRequest() {
        TaskEditNotAllowedException ex = new TaskEditNotAllowedException("task-id-55");

        ResponseEntity<Map<String, String>> response = handler.handleTaskEditNotAllowedException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("task-id-55"));
    }
}

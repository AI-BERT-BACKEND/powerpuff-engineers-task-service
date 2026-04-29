package com.aibert.dosw.domain.exceptions;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(String taskId) {
        super("Tarea no encontrada con ID: " + taskId);
    }
}

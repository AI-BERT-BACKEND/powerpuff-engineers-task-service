package com.aibert.dosw.domain.exceptions;

public class TaskForbiddenException extends RuntimeException {

    public TaskForbiddenException(String taskId) {
        super("No tienes permiso para acceder a la tarea con ID: " + taskId);
    }
}

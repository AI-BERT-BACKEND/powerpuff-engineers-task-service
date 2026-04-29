package com.aibert.dosw.domain.exceptions;

public class TaskConflictException extends RuntimeException {
    public TaskConflictException(String message) {
        super(message);
    }
}

package com.aibert.dosw.domain.exceptions;

public class TaskEditNotAllowedException extends RuntimeException {

    public TaskEditNotAllowedException(String message) {
        super(message);
    }
}

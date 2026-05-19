package com.aibert.dosw.domain.model;

/**
 * Represents the lifecycle status of a task.
 * Tasks progress from {@code TODO} → {@code IN_PROGRESS} → {@code COMPLETED}.
 */
public enum TaskStatus {
    /** The task has been created but work has not yet started. */
    TODO,
    /** The task is currently being worked on. */
    IN_PROGRESS,
    /** The task has been paused. */
    PAUSED,
    /** The task has been finished. {@code completedAt} is recorded automatically. */
    COMPLETED
}

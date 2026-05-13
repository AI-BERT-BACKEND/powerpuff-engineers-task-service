package com.aibert.dosw.domain.model;

/**
 * Defines the urgency level of a task.
 * Used to sort and schedule tasks automatically.
 * Tasks without a priority are assigned {@code MEDIUM} by default.
 */
public enum TaskPriority {
    /** Low urgency; can be deferred without immediate concern. */
    LOW,
    /** Default priority assigned when none is specified. */
    MEDIUM,
    /** High urgency; may be escalated automatically when the deadline is within 24 hours. */
    HIGH,
    /** Maximum urgency; will not be auto-escalated further. */
    CRITICAL
}

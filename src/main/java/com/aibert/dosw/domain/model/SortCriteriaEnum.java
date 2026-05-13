package com.aibert.dosw.domain.model;

/**
 * Defines the criteria by which tasks can be sorted when retrieved.
 */
public enum SortCriteriaEnum {
    /** Sort by task priority in descending order (CRITICAL first). */
    PRIORITY,
    /** Sort by deadline in ascending order (earliest first). */
    DEADLINE,
    /** Sort alphabetically by subject identifier. */
    SUBJECT
}

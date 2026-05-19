package com.aibert.dosw.domain.model;

/**
 * Classifies the nature of an academic task.
 * Used to distinguish between assignments, exams, projects,
 * reading activities, and other miscellaneous work.
 */
public enum TaskType {
    /** A regular assignment or homework task. */
    TAREA,
    /** An exam or evaluation. */
    EXAMEN,
    /** A project that typically spans multiple sessions. */
    PROYECTO,
    /** A short quiz or pop-test. */
    QUIZ,
    /** A reading activity or literature review. */
    LECTURA,
    /** Any other type of academic task not covered by the above. */
    OTRO
}

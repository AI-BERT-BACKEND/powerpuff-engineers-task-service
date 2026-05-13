package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Output port defining the persistence contract for tasks.
 * Implemented by {@code InMemoryTaskRepository} (profile: inmemory) and
 * {@code TaskRepositoryAdapter} (profile: postgres).
 */
public interface TaskRepositoryPort {

    /**
     * Persists a task. Generates a UUID if the task has no ID.
     *
     * @param task the task to save
     * @return the saved task (with ID set)
     */
    Task save(Task task);

    /**
     * Checks whether a task with the same student, subject, and title already exists.
     * The title comparison is case-insensitive.
     *
     * @param studentId the student's identifier
     * @param subjectId the subject's identifier
     * @param title     the task title to check
     * @return {@code true} if a duplicate exists, {@code false} otherwise
     */
    boolean existsDuplicate(String studentId, String subjectId, String title);

    /**
     * Returns all tasks belonging to the given student.
     *
     * @param studentId the student's identifier
     * @return list of tasks; empty if none found
     */
    List<Task> findByStudentId(String studentId);

    /**
     * Persists a batch of tasks, generating IDs for any that lack one.
     *
     * @param tasks the tasks to save
     * @return the list of saved tasks
     */
    List<Task> saveAll(List<Task> tasks);

    /**
     * Finds a single task by its unique identifier.
     *
     * @param taskId the task identifier
     * @return an {@link Optional} containing the task if found, or empty otherwise
     */
    Optional<Task> findById(String taskId);

    /**
     * Retrieves tasks for a student with optional filters.
     * All filter parameters are optional; passing {@code null} disables the corresponding filter.
     *
     * @param studentId the student's identifier
     * @param status    optional status filter
     * @param startDate optional lower bound for the deadline (inclusive)
     * @param endDate   optional upper bound for the deadline (inclusive)
     * @param subjectId optional subject identifier filter
     * @param taskType  optional task type filter
     * @return filtered task list
     */
    List<Task> findByStudentIdWithFilters(String studentId, TaskStatus status,
                                          LocalDateTime startDate, LocalDateTime endDate,
                                          String subjectId, TaskType taskType);

    /**
     * Permanently deletes the task with the given identifier.
     * No-op if no task exists with that ID.
     *
     * @param taskId the identifier of the task to delete
     */
    void deleteById(String taskId);
}

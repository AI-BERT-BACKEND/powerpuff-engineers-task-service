package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.infrastructure.adapters.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link TaskEntity}.
 * Extends {@link JpaRepository} to provide standard CRUD operations
 * in addition to the domain-specific query methods declared below.
 */
public interface TaskJpaRepository extends JpaRepository<TaskEntity, String> {

    /**
     * Returns all task entities belonging to the given student.
     *
     * @param studentId the student's identifier
     * @return list of entities; empty if none found
     */
    List<TaskEntity> findByStudentId(String studentId);

    /**
     * Checks whether a task with the same student, subject, and title already exists.
     * The title comparison is case-insensitive.
     *
     * @param studentId the student's identifier
     * @param subjectId the subject's identifier
     * @param title     the task title to check
     * @return {@code true} if a matching entity exists
     */
    boolean existsByStudentIdAndSubjectIdAndTitleIgnoreCase(String studentId, String subjectId, String title);

    /**
     * Finds a task entity by its ID, regardless of whether it has been soft-deleted.
     * This bypasses the {@code @SQLRestriction("deleted_at IS NULL")} filter on {@link TaskEntity}.
     *
     * @param taskId the task identifier
     * @return the entity if found, even when {@code deleted_at} is set
     */
    @Query(value = "SELECT * FROM tasks WHERE id = ?1", nativeQuery = true)
    Optional<TaskEntity> findByIdIncludingDeleted(String taskId);
}

package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.infrastructure.adapters.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

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
}

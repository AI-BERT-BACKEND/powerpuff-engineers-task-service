package com.aibert.dosw.infrastructure.external;

import com.aibert.dosw.domain.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskJpaRepository extends JpaRepository<TaskEntity, String> {

    boolean existsByStudentIdAndSubjectIdAndTitleIgnoreCase(String studentId, String subjectId, String title);

    List<TaskEntity> findByStudentId(String studentId);

    /**
     * Retrieves tasks for a student with optional filters.
     * Null params are treated as "no filter" (all values match).
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.studentId = :studentId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:startDate IS NULL OR t.deadline >= :startDate) " +
           "AND (:endDate IS NULL OR t.deadline <= :endDate)")
    List<TaskEntity> findByStudentIdWithFilters(
            @Param("studentId") String studentId,
            @Param("status") TaskStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}

package com.aibert.dosw.infrastructure.adapters.persistence.repository;

import com.aibert.dosw.infrastructure.adapters.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskJpaRepository extends JpaRepository<TaskEntity, String> {

    List<TaskEntity> findByStudentId(String studentId);

    boolean existsByStudentIdAndSubjectIdAndTitleIgnoreCase(String studentId, String subjectId, String title);
}

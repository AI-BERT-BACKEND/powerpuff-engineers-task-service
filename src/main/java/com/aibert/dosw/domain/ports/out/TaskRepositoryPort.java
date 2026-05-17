package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepositoryPort {

    Task save(Task task);

    boolean existsDuplicate(String studentId, String subjectId, String title);

    List<Task> findByStudentId(String studentId);

    List<Task> saveAll(List<Task> tasks);

    Optional<Task> findById(String taskId);

    void deleteById(String taskId);

    List<Task> findByStudentIdWithFilters(String studentId, TaskStatus status,
                                          LocalDateTime startDate, LocalDateTime endDate);
}

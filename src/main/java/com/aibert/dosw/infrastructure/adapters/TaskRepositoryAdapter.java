package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import com.aibert.dosw.infrastructure.external.TaskEntity;
import com.aibert.dosw.infrastructure.external.TaskJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TaskRepositoryAdapter implements TaskRepositoryPort {

    private final TaskJpaRepository taskJpaRepository;
    private final TaskEntityMapper taskEntityMapper;

    @Override
    public Task save(Task task) {
        TaskEntity entity = taskEntityMapper.toEntity(task);
        TaskEntity savedEntity = taskJpaRepository.save(entity);
        return taskEntityMapper.toModel(savedEntity);
    }

    @Override
    public boolean existsDuplicate(String studentId, String subjectId, String title) {
        return taskJpaRepository.existsByStudentIdAndSubjectIdAndTitleIgnoreCase(studentId, subjectId, title);
    }

    @Override
    public List<Task> findByStudentId(String studentId) {
        return taskJpaRepository.findByStudentId(studentId).stream()
                .map(taskEntityMapper::toModel)
                .toList();
    }

    @Override
    public List<Task> saveAll(List<Task> tasks) {
        List<TaskEntity> entities = tasks.stream()
                .map(taskEntityMapper::toEntity)
                .toList();
        return taskJpaRepository.saveAll(entities).stream()
                .map(taskEntityMapper::toModel)
                .toList();
    }

    @Override
    public Optional<Task> findById(String taskId) {
        return taskJpaRepository.findById(taskId)
                .map(taskEntityMapper::toModel);
    }

    @Override
    public List<Task> findByStudentIdWithFilters(String studentId, TaskStatus status,
                                                  LocalDateTime startDate, LocalDateTime endDate) {
        return taskJpaRepository.findByStudentIdWithFilters(studentId, status, startDate, endDate)
                .stream()
                .map(taskEntityMapper::toModel)
                .toList();
    }
}


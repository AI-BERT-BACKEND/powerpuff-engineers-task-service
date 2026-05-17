package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import com.aibert.dosw.infrastructure.adapters.persistence.mapper.TaskEntityMapper;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.TaskJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Profile("postgres")
@RequiredArgsConstructor
public class TaskRepositoryAdapter implements TaskRepositoryPort {

    private final TaskJpaRepository jpaRepository;
    private final TaskEntityMapper mapper;

    @Override
    public Task save(Task task) {
        if (task.getId() == null || task.getId().isBlank()) {
            task.setId(UUID.randomUUID().toString());
        }
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(task)));
    }

    @Override
    public boolean existsDuplicate(String studentId, String subjectId, String title) {
        return jpaRepository.existsByStudentIdAndSubjectIdAndTitleIgnoreCase(studentId, subjectId, title);
    }

    @Override
    public List<Task> findByStudentId(String studentId) {
        return jpaRepository.findByStudentId(studentId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Task> saveAll(List<Task> tasks) {
        tasks.forEach(t -> {
            if (t.getId() == null || t.getId().isBlank()) {
                t.setId(UUID.randomUUID().toString());
            }
        });
        return jpaRepository.saveAll(tasks.stream().map(mapper::toEntity).toList())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Task> findById(String taskId) {
        return jpaRepository.findById(taskId).map(mapper::toDomain);
    }

    @Override
    public void deleteById(String taskId) {
        jpaRepository.deleteById(taskId);
    }

    @Override
    public List<Task> findByStudentIdWithFilters(String studentId, TaskStatus status,
                                                  LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByStudentId(studentId).stream()
                .filter(t -> status == null || status.equals(t.getStatus()))
                .filter(t -> startDate == null || (t.getDeadline() != null && !t.getDeadline().isBefore(startDate)))
                .filter(t -> endDate == null || (t.getDeadline() != null && !t.getDeadline().isAfter(endDate)))
                .map(mapper::toDomain)
                .toList();
    }
}

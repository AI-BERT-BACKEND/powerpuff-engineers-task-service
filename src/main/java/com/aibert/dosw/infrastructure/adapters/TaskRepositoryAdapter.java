package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;
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

/**
 * JPA-backed implementation of {@link TaskRepositoryPort}, active under the {@code postgres} Spring profile.
 * Translates domain {@link Task} objects to/from JPA entities using {@code TaskEntityMapper}.
 */
@Component
@Profile("postgres")
@RequiredArgsConstructor
public class TaskRepositoryAdapter implements TaskRepositoryPort {

    private final TaskJpaRepository jpaRepository;
    private final TaskEntityMapper mapper;

    /**
     * {@inheritDoc}
     * Generates a random UUID as the task ID if none is set.
     */
    @Override
    public Task save(Task task) {
        if (task.getId() == null || task.getId().isBlank()) {
            task.setId(UUID.randomUUID().toString());
        }
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(task)));
    }

    /**
     * {@inheritDoc}
     * Delegates to {@code TaskJpaRepository.existsByStudentIdAndSubjectIdAndTitleIgnoreCase}.
     */
    @Override
    public boolean existsDuplicate(String studentId, String subjectId, String title) {
        return jpaRepository.existsByStudentIdAndSubjectIdAndTitleIgnoreCase(studentId, subjectId, title);
    }

    /**
     * {@inheritDoc}
     * Fetches all JPA entities for the student and maps them to domain objects.
     */
    @Override
    public List<Task> findByStudentId(String studentId) {
        return jpaRepository.findByStudentId(studentId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * {@inheritDoc}
     * Assigns a random UUID to each task in the list that has no ID before persisting.
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Task> findById(String taskId) {
        return jpaRepository.findById(taskId).map(mapper::toDomain);
    }

    /**
     * {@inheritDoc}
     * Applies all non-null filters in-memory on the result of
     * {@code findByStudentId}. Tasks with a {@code null} deadline are
     * excluded when date filters are active.
     */
    @Override
    public List<Task> findByStudentIdWithFilters(String studentId, TaskStatus status,
                                                  LocalDateTime startDate, LocalDateTime endDate,
                                                  String subjectId, TaskType taskType) {
        return jpaRepository.findByStudentId(studentId).stream()
                .filter(t -> status == null || status.equals(t.getStatus()))
                .filter(t -> startDate == null || (t.getDeadline() != null && !t.getDeadline().isBefore(startDate)))
                .filter(t -> endDate == null || (t.getDeadline() != null && !t.getDeadline().isAfter(endDate)))
                .filter(t -> subjectId == null || subjectId.equals(t.getSubjectId()))
                .filter(t -> taskType == null || taskType.equals(t.getTaskType()))
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * {@inheritDoc}
     * Delegates directly to {@link org.springframework.data.jpa.repository.JpaRepository#deleteById}.
     */
    @Override
    public void deleteById(String taskId) {
        jpaRepository.deleteById(taskId);
    }
}

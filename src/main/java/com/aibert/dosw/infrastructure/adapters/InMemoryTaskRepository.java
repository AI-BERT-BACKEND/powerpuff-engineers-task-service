package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.model.TaskType;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link TaskRepositoryPort} backed by a {@link java.util.concurrent.ConcurrentHashMap}.
 * Active under the {@code inmemory} Spring profile, primarily for testing and local development.
 */
@Component
@Profile("inmemory")
public class InMemoryTaskRepository implements TaskRepositoryPort {

    private final Map<String, Task> store = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     * Generates a random UUID as the task ID if none is set.
     */
    @Override
    public Task save(Task task) {
        if (task.getId() == null || task.getId().isBlank()) {
            task.setId(UUID.randomUUID().toString());
        }
        store.put(task.getId(), task);
        return task;
    }

    /**
     * {@inheritDoc}
     * Performs a case-insensitive title comparison using {@link String#equalsIgnoreCase}.
     */
    @Override
    public boolean existsDuplicate(String studentId, String subjectId, String title) {
        return store.values().stream()
                .anyMatch(t -> t.getStudentId().equals(studentId)
                        && t.getSubjectId().equals(subjectId)
                        && t.getTitle().equalsIgnoreCase(title));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Task> findByStudentId(String studentId) {
        return store.values().stream()
                .filter(t -> studentId.equals(t.getStudentId()))
                .toList();
    }

    /**
     * {@inheritDoc}
     * Delegates to {@link #save(Task)} for each task in the list.
     */
    @Override
    public List<Task> saveAll(List<Task> tasks) {
        List<Task> saved = new ArrayList<>();
        for (Task task : tasks) {
            saved.add(save(task));
        }
        return saved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Task> findById(String taskId) {
        return Optional.ofNullable(store.get(taskId));
    }

    /**
     * {@inheritDoc}
     * Tasks with a {@code null} deadline are excluded when date-range filters are active.
     */
    @Override
    public List<Task> findByStudentIdWithFilters(String studentId, TaskStatus status,
                                                  LocalDateTime startDate, LocalDateTime endDate,
                                                  String subjectId, TaskType taskType) {
        return store.values().stream()
                .filter(t -> studentId.equals(t.getStudentId()))
                .filter(t -> status == null || status.equals(t.getStatus()))
                .filter(t -> startDate == null || (t.getDeadline() != null && !t.getDeadline().isBefore(startDate)))
                .filter(t -> endDate == null || (t.getDeadline() != null && !t.getDeadline().isAfter(endDate)))
                .filter(t -> subjectId == null || subjectId.equals(t.getSubjectId()))
                .filter(t -> taskType == null || taskType.equals(t.getTaskType()))
                .toList();
    }

    /**
     * {@inheritDoc}
     * Removes the entry from the in-memory store. No-op if absent.
     */
    @Override
    public void deleteById(String taskId) {
        store.remove(taskId);
    }
}

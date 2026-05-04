package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
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

@Component
@Profile("inmemory")
public class InMemoryTaskRepository implements TaskRepositoryPort {

    private final Map<String, Task> store = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        if (task.getId() == null || task.getId().isBlank()) {
            task.setId(UUID.randomUUID().toString());
        }
        store.put(task.getId(), task);
        return task;
    }

    @Override
    public boolean existsDuplicate(String studentId, String subjectId, String title) {
        return store.values().stream()
                .anyMatch(t -> t.getStudentId().equals(studentId)
                        && t.getSubjectId().equals(subjectId)
                        && t.getTitle().equalsIgnoreCase(title));
    }

    @Override
    public List<Task> findByStudentId(String studentId) {
        return store.values().stream()
                .filter(t -> studentId.equals(t.getStudentId()))
                .toList();
    }

    @Override
    public List<Task> saveAll(List<Task> tasks) {
        List<Task> saved = new ArrayList<>();
        for (Task task : tasks) {
            saved.add(save(task));
        }
        return saved;
    }

    @Override
    public Optional<Task> findById(String taskId) {
        return Optional.ofNullable(store.get(taskId));
    }

    @Override
    public List<Task> findByStudentIdWithFilters(String studentId, TaskStatus status,
                                                  LocalDateTime startDate, LocalDateTime endDate) {
        return store.values().stream()
                .filter(t -> studentId.equals(t.getStudentId()))
                .filter(t -> status == null || status.equals(t.getStatus()))
                .filter(t -> startDate == null || (t.getDeadline() != null && !t.getDeadline().isBefore(startDate)))
                .filter(t -> endDate == null || (t.getDeadline() != null && !t.getDeadline().isAfter(endDate)))
                .toList();
    }
}

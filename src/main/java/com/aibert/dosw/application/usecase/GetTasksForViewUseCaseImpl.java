package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.GetTasksForViewUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetTasksForViewUseCaseImpl implements GetTasksForViewUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    @Override
    public Map<TaskStatus, List<Task>> getKanbanView(String studentId) {
        List<Task> allTasks = taskRepositoryPort.findByStudentId(studentId);

        return allTasks.stream()
                .filter(t -> t.getStatus() != null)
                .collect(Collectors.groupingBy(Task::getStatus));
    }

    @Override
    public List<Task> getCalendarView(String studentId, TaskStatus status,
                                      LocalDateTime startDate, LocalDateTime endDate) {
        return taskRepositoryPort.findByStudentIdWithFilters(studentId, status, startDate, endDate);
    }
}

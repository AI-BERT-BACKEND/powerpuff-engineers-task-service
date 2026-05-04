package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface GetTasksForViewUseCase {

    Map<TaskStatus, List<Task>> getKanbanView(String studentId);

    List<Task> getCalendarView(String studentId, TaskStatus status,
                               LocalDateTime startDate, LocalDateTime endDate);
}

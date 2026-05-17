package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;
import java.util.List;

public interface GetTasksUseCase {
    List<Task> getTasksByStudentId(String studentId);
    Task getTaskById(String taskId);
}

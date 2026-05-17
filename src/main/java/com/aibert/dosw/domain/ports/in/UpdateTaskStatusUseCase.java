package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;

public interface UpdateTaskStatusUseCase {
    Task updateStatus(String taskId, String userId, TaskStatus newStatus);
}

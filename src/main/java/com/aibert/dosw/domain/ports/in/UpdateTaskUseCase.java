package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.request.UpdateTaskRequest;
import com.aibert.dosw.domain.model.Task;

public interface UpdateTaskUseCase {

    Task updateTask(String taskId, String userId, UpdateTaskRequest request);
}

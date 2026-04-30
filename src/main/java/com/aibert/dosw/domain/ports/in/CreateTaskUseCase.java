package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;

public interface CreateTaskUseCase {
    Task createTask(Task task);
}

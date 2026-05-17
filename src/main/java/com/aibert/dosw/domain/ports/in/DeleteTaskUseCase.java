package com.aibert.dosw.domain.ports.in;

public interface DeleteTaskUseCase {

    void deleteTask(String taskId, String userId);
}

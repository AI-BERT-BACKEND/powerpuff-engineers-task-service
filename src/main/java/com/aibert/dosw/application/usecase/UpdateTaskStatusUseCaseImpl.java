package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.UpdateTaskStatusUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateTaskStatusUseCaseImpl implements UpdateTaskStatusUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    @Override
    public Task updateStatus(String taskId, TaskStatus newStatus) {
        Task task = taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        task.setStatus(newStatus);

        if (TaskStatus.COMPLETED.equals(newStatus)) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }

        return taskRepositoryPort.save(task);
    }
}

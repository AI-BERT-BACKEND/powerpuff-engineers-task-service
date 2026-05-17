package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.ports.in.GetTasksUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTasksUseCaseImpl implements GetTasksUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    @Override
    public List<Task> getTasksByStudentId(String studentId) {
        return taskRepositoryPort.findByStudentId(studentId);
    }

    @Override
    public Task getTaskById(String taskId) {
        return taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }
}

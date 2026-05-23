package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.TaskNotFoundException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.ports.in.GetTasksUseCase;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring service implementing the {@link GetTasksUseCase} input port.
 * Delegates retrieval to the persistence output port without additional business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetTasksUseCaseImpl implements GetTasksUseCase {

    private final TaskRepositoryPort taskRepositoryPort;

    /**
     * {@inheritDoc}
     *
     * @param studentId the student's identifier
     * @return all tasks belonging to the student; empty list if none found
     */
    @Override
    public List<Task> getTasksByStudentId(String studentId) {
        List<Task> tasks = taskRepositoryPort.findByStudentId(studentId);
        log.debug("TASKS_FETCHED | operation=GET_BY_STUDENT | studentId={} | count={}", studentId, tasks.size());
        return tasks;
    }

    @Override
    public Task getTaskById(String taskId) {
        return taskRepositoryPort.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }
}

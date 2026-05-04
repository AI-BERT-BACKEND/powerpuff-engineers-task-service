package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.exceptions.SubjectNotFoundException;
import com.aibert.dosw.domain.exceptions.TaskConflictException;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.CreateTaskUseCase;
import com.aibert.dosw.domain.ports.out.SubjectValidationPort;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateTaskUseCaseImpl implements CreateTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private final SubjectValidationPort subjectValidationPort;

    @Override
    public Task createTask(Task task) {
        if (!subjectValidationPort.exists(task.getSubjectId())) {
            throw new SubjectNotFoundException("La materia " + task.getSubjectId() + " no existe.");
        }

        if (taskRepositoryPort.existsDuplicate(task.getStudentId(), task.getSubjectId(), task.getTitle())) {
            throw new TaskConflictException("Ya existe una tarea con el título '" + task.getTitle() + "' para esta materia.");
        }

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        return taskRepositoryPort.save(task);
    }
}

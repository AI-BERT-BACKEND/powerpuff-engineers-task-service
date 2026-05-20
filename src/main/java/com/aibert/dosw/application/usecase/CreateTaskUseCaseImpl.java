package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.request.TaskNotificationEvent;
import com.aibert.dosw.domain.exceptions.SubjectNotFoundException;
import com.aibert.dosw.domain.exceptions.SubjectNotInActiveSemesterException;
import com.aibert.dosw.domain.exceptions.TaskConflictException;
import com.aibert.dosw.domain.model.NotificationEventType;
import com.aibert.dosw.domain.model.NotificationSeverity;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.in.CreateTaskUseCase;
import com.aibert.dosw.domain.ports.out.SubjectValidationPort;
import com.aibert.dosw.domain.ports.out.TaskNotificationPort;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTaskUseCaseImpl implements CreateTaskUseCase {

    private final TaskRepositoryPort taskRepositoryPort;
    private final SubjectValidationPort subjectValidationPort;
    private final TaskNotificationPort taskNotificationPort;

    private static final int DEADLINE_URGENCY_HOURS = 24;

    @Override
    public Task createTask(Task task) {
        if (!subjectValidationPort.exists(task.getSubjectId(), task.getStudentId())) {
            throw new SubjectNotFoundException("La materia " + task.getSubjectId() + " no existe.");
        }

        if (!subjectValidationPort.isInActiveSemester(task.getSubjectId(), task.getStudentId())) {
            throw new SubjectNotInActiveSemesterException(task.getSubjectId());
        }

        if (taskRepositoryPort.existsDuplicate(task.getStudentId(), task.getSubjectId(), task.getTitle())) {
            throw new TaskConflictException("Ya existe una tarea con el título '" + task.getTitle() + "' para esta materia.");
        }

        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        escalateIfUrgent(task);

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }

        Task saved = taskRepositoryPort.save(task);
        log.info("AUDIT | operation=CREATE | studentId={} | taskId={} | title={} | createdAt={}",
                saved.getStudentId(), saved.getId(), saved.getTitle(), LocalDateTime.now());

        recalculatePriorityForActiveTasks(saved.getStudentId(), saved.getId());

        taskNotificationPort.publish(new TaskNotificationEvent(
                saved.getStudentId(),
                NotificationEventType.TASK_REMINDER,
                "Tienes tareas pendientes",
                "La tarea '" + saved.getTitle() + "' ha sido creada y está pendiente.",
                toNotificationSeverity(saved.getPriority()),
                saved.getId()
        ));

        return saved;
    }

    private void escalateIfUrgent(Task task) {
        if (task.getDeadline() == null) return;
        long hoursUntilDeadline = ChronoUnit.HOURS.between(LocalDateTime.now(), task.getDeadline());
        boolean isUrgent = hoursUntilDeadline >= 0 && hoursUntilDeadline <= DEADLINE_URGENCY_HOURS;
        boolean canEscalate = task.getPriority() != TaskPriority.HIGH
                && task.getPriority() != TaskPriority.CRITICAL;
        if (isUrgent && canEscalate) {
            task.setPriority(TaskPriority.HIGH);
        }
    }

    private NotificationSeverity toNotificationSeverity(TaskPriority priority) {
        if (priority == null) return NotificationSeverity.LOW;
        return switch (priority) {
            case CRITICAL -> NotificationSeverity.CRITICAL;
            case HIGH     -> NotificationSeverity.HIGH;
            case MEDIUM   -> NotificationSeverity.MEDIUM;
            default       -> NotificationSeverity.LOW;
        };
    }

    private void recalculatePriorityForActiveTasks(String studentId, String excludeTaskId) {
        List<Task> activeTasks = taskRepositoryPort.findByStudentId(studentId).stream()
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .filter(t -> !t.getId().equals(excludeTaskId))
                .toList();

        LocalDateTime now = LocalDateTime.now();
        boolean anyUpdated = false;

        for (Task t : activeTasks) {
            if (t.getDeadline() == null) continue;
            long hoursUntilDeadline = ChronoUnit.HOURS.between(now, t.getDeadline());
            boolean isUrgent = hoursUntilDeadline >= 0 && hoursUntilDeadline <= DEADLINE_URGENCY_HOURS;
            boolean canEscalate = t.getPriority() != TaskPriority.HIGH
                    && t.getPriority() != TaskPriority.CRITICAL;
            if (isUrgent && canEscalate) {
                t.setPriority(TaskPriority.HIGH);
                anyUpdated = true;
            }
        }

        if (anyUpdated) {
            taskRepositoryPort.saveAll(activeTasks);
        }
    }
}

package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;

import java.time.LocalDateTime;

/**
 * Input port for rescheduling a task to a new date (R17 — drag-and-drop calendar).
 * Only updates {@code scheduledDate}; all other fields remain unchanged.
 */
public interface RescheduleTaskUseCase {

    /**
     * Moves the task identified by {@code taskId} to {@code newScheduledDate}.
     *
     * <p>Business rules enforced:</p>
     * <ul>
     *   <li>RN-01: only the task owner may reschedule it.</li>
     *   <li>RN-R17-01: {@code newScheduledDate} must not be after the task's {@code deadline}.</li>
     *   <li>RN-R17-02: the new time block must not overlap with another scheduled task of the same student.</li>
     * </ul>
     *
     * @param taskId           the identifier of the task to reschedule
     * @param studentId        the identifier of the requesting student (ownership check)
     * @param newScheduledDate the new start date/time for the task
     * @return the updated and persisted task
     * @throws com.aibert.dosw.domain.exceptions.TaskNotFoundException  if the task does not exist
     * @throws com.aibert.dosw.domain.exceptions.TaskForbiddenException if the student does not own the task
     * @throws com.aibert.dosw.domain.exceptions.TaskConflictException  if {@code newScheduledDate} exceeds
     *                                                                    the deadline or overlaps another task
     */
    Task rescheduleTask(String taskId, String studentId, LocalDateTime newScheduledDate);
}

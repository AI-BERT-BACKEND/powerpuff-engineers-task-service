package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;

import java.time.LocalDateTime;

/**
 * Input port for updating a task's deadline via calendar drag-and-drop (AIB-21 RN-02).
 * After the deadline is changed, a priority recalculation is triggered.
 */
public interface UpdateDeadlineUseCase {

    /**
     * Updates the deadline of the specified task and recalculates its priority.
     *
     * @param taskId      the task identifier
     * @param studentId   the requesting student's identifier (ownership enforced)
     * @param newDeadline the new deadline date-time
     * @return the updated and persisted task
     */
    Task updateDeadline(String taskId, String studentId, LocalDateTime newDeadline);
}

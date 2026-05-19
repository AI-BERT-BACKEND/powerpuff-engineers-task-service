package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;
import java.util.List;

/**
 * Input port for retrieving the raw (unordered) task list of a student.
 */
public interface GetTasksUseCase {

    /**
     * Returns all tasks belonging to the given student.
     *
     * @param studentId the student's identifier (forwarded from the X-User-Id header)
     * @return a list of tasks; empty if the student has no tasks
     */
    List<Task> getTasksByStudentId(String studentId);
    Task getTaskById(String taskId);
}

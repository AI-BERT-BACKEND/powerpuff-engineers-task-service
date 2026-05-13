package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.Task;
import java.util.List;

/**
 * Input port for the automatic task scheduling use case.
 * Assigns a {@code scheduledDate} to each pending task based on priority and available time per day.
 */
public interface OrganizeTasksUseCase {

    /**
     * Schedules all {@code TODO} tasks for the given student.
     * <p>Tasks are sorted by priority (highest first) and then by deadline.
     * Each task receives a {@code scheduledDate} at 09:00 on the earliest
     * available day that has not exceeded {@code MAX_MINUTES_PER_DAY}.</p>
     *
     * @param studentId the student whose tasks should be organized
     * @return all tasks for the student (both scheduled and non-{@code TODO} tasks)
     */
    List<Task> organizeTasksForStudent(String studentId);
}

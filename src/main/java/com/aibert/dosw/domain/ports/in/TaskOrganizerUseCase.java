package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.SortCriteriaEnum;
import com.aibert.dosw.domain.model.Task;
import java.util.List;

/**
 * Input port for retrieving sorted task lists with optional priority escalation.
 */
public interface TaskOrganizerUseCase {

    /**
     * Returns all tasks for a student sorted by the given criteria.
     * <p>Before sorting, tasks whose deadline is within 24 hours are automatically
     * escalated to {@code HIGH} priority (unless already {@code HIGH} or {@code CRITICAL}).</p>
     *
     * @param studentId    the student's identifier
     * @param sortCriteria the desired sort order; defaults to {@code PRIORITY} when {@code null}
     * @return sorted task list
     */
    List<Task> getOrganizedTasks(String studentId, SortCriteriaEnum sortCriteria);
    List<Task> getOrganizedTasks(String studentId, SortCriteriaEnum sortCriteria, Integer limit);

    /**
     * Returns active tasks sorted by priority. Result is cached per student;
     * pass {@code forzarRecalculo=true} to bypass the cache and recompute.
     */
    List<Task> getPrioritizedActiveTasks(String studentId, boolean forzarRecalculo);
    List<Task> getPrioritizedActiveTasks(String studentId);
}

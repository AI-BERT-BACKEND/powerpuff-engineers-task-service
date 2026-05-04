package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.domain.model.SortCriteriaEnum;
import com.aibert.dosw.domain.model.Task;
import java.util.List;

public interface TaskOrganizerUseCase {
    List<Task> getOrganizedTasks(String studentId, SortCriteriaEnum sortCriteria);
}

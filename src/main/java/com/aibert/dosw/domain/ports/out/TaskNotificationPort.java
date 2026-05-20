package com.aibert.dosw.domain.ports.out;

import com.aibert.dosw.application.dto.request.TaskNotificationEvent;

public interface TaskNotificationPort {
    void publish(TaskNotificationEvent event);
}

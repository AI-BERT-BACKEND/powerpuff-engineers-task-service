package com.aibert.dosw.application.dto.request;

import com.aibert.dosw.domain.model.NotificationEventType;
import com.aibert.dosw.domain.model.NotificationSeverity;

public record TaskNotificationEvent(
        String userId,
        NotificationEventType type,
        String title,
        String message,
        NotificationSeverity severity,
        String relatedEntityId
) {}

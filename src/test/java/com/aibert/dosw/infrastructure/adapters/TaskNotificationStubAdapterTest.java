package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.application.dto.request.TaskNotificationEvent;
import com.aibert.dosw.domain.model.NotificationEventType;
import com.aibert.dosw.domain.model.NotificationSeverity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TaskNotificationStubAdapterTest {

    private final TaskNotificationStubAdapter adapter = new TaskNotificationStubAdapter();

    @Test
    void publish_ShouldNotThrow() {
        TaskNotificationEvent event = new TaskNotificationEvent(
                "user-1",
                NotificationEventType.TASK_REMINDER,
                "Tarea pendiente",
                "La tarea X ha sido creada.",
                NotificationSeverity.MEDIUM,
                "task-123"
        );
        assertDoesNotThrow(() -> adapter.publish(event));
    }

    @Test
    void publish_WhenUserIdNull_ShouldNotThrow() {
        TaskNotificationEvent event = new TaskNotificationEvent(
                null,
                NotificationEventType.OVERLOAD_ALERT,
                "Sobrecarga",
                "Tienes demasiadas tareas activas.",
                NotificationSeverity.HIGH,
                "task-456"
        );
        assertDoesNotThrow(() -> adapter.publish(event));
    }

    @Test
    void publish_WhenAllFieldsPresent_ShouldCompleteWithoutException() {
        TaskNotificationEvent event = new TaskNotificationEvent(
                "student-99",
                NotificationEventType.OVERLOAD_ALERT,
                "Alerta",
                "Mensaje de alerta.",
                NotificationSeverity.CRITICAL,
                "task-789"
        );
        assertDoesNotThrow(() -> adapter.publish(event));
    }
}

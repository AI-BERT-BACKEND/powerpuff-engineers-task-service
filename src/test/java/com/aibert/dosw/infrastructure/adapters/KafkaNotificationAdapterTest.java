package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.application.dto.request.TaskNotificationEvent;
import com.aibert.dosw.domain.model.NotificationEventType;
import com.aibert.dosw.domain.model.NotificationSeverity;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaNotificationAdapterTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "task.notifications";

    @Test
    void publish_WhenSuccess_ShouldSendEventWithUserIdAsKey() {
        KafkaNotificationAdapter adapter = new KafkaNotificationAdapter(kafkaTemplate, TOPIC);
        TaskNotificationEvent event = new TaskNotificationEvent(
                "user-1", NotificationEventType.TASK_REMINDER,
                "Título", "Mensaje", NotificationSeverity.MEDIUM, "task-1");

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(TOPIC, 0), 0L, 0, System.currentTimeMillis(), 0, 0);
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(TOPIC, "user-1", event);
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);

        when(kafkaTemplate.send(TOPIC, "user-1", event))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        assertDoesNotThrow(() -> adapter.publish(event));
        verify(kafkaTemplate).send(TOPIC, "user-1", event);
    }

    @Test
    void publish_WhenUserIdNull_ShouldUseEmptyStringAsKey() {
        KafkaNotificationAdapter adapter = new KafkaNotificationAdapter(kafkaTemplate, TOPIC);
        TaskNotificationEvent event = new TaskNotificationEvent(
                null, NotificationEventType.OVERLOAD_ALERT,
                "Alerta", "Sobrecarga", NotificationSeverity.HIGH, "task-2");

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(TOPIC, 0), 0L, 0, System.currentTimeMillis(), 0, 0);
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(TOPIC, "", event);
        SendResult<String, Object> sendResult = new SendResult<>(producerRecord, metadata);

        when(kafkaTemplate.send(TOPIC, "", event))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        assertDoesNotThrow(() -> adapter.publish(event));
        verify(kafkaTemplate).send(TOPIC, "", event);
    }

    @Test
    void publish_WhenKafkaFails_ShouldLogErrorWithoutThrowing() {
        KafkaNotificationAdapter adapter = new KafkaNotificationAdapter(kafkaTemplate, TOPIC);
        TaskNotificationEvent event = new TaskNotificationEvent(
                "user-1", NotificationEventType.TASK_REMINDER,
                "Título", "Mensaje", NotificationSeverity.LOW, "task-3");

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Broker no disponible"));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(failedFuture);

        assertDoesNotThrow(() -> adapter.publish(event));
    }

    @Test
    void publish_WhenCriticalSeverity_ShouldSendWithoutThrowing() {
        KafkaNotificationAdapter adapter = new KafkaNotificationAdapter(kafkaTemplate, TOPIC);
        TaskNotificationEvent event = new TaskNotificationEvent(
                "user-2", NotificationEventType.OVERLOAD_ALERT,
                "Crítico", "Tarea crítica pendiente", NotificationSeverity.CRITICAL, "task-4");

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("test"));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);

        assertDoesNotThrow(() -> adapter.publish(event));
    }
}

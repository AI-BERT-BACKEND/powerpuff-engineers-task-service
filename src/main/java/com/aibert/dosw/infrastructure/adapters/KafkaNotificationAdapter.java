package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.application.dto.request.TaskNotificationEvent;
import com.aibert.dosw.domain.ports.out.TaskNotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("kafka")
public class KafkaNotificationAdapter implements TaskNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaNotificationAdapter.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaNotificationAdapter(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.topics.task-notifications}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(TaskNotificationEvent event) {
        String key = event.userId() != null ? event.userId() : "";
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish notification event: type={} userId={} error={}",
                                event.type(), event.userId(), ex.getMessage());
                    } else {
                        log.info("Notification event published: type={} userId={} partition={} offset={}",
                                event.type(), event.userId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}

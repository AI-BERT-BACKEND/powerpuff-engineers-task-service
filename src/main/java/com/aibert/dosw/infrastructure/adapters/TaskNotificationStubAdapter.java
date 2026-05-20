package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.application.dto.request.TaskNotificationEvent;
import com.aibert.dosw.domain.ports.out.TaskNotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!kafka")
public class TaskNotificationStubAdapter implements TaskNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(TaskNotificationStubAdapter.class);

    @Override
    public void publish(TaskNotificationEvent event) {
        log.debug("[STUB] Notification event skipped (kafka profile inactive): type={} userId={} relatedEntityId={}",
                event.type(), event.userId(), event.relatedEntityId());
    }
}

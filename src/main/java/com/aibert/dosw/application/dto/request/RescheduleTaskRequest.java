package com.aibert.dosw.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request payload for the drag-and-drop reschedule endpoint (R17).
 * Contains only the new {@code scheduledDate}; all other task fields remain unchanged.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleTaskRequest {

    @NotNull(message = "scheduledDate is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledDate;
}

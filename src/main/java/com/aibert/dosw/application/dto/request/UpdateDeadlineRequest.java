package com.aibert.dosw.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request payload for the calendar drag-and-drop deadline update endpoint (AIB-21).
 * Contains only the new {@code newDeadline}; all other task fields remain unchanged.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeadlineRequest {

    @NotNull(message = "newDeadline is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime newDeadline;
}

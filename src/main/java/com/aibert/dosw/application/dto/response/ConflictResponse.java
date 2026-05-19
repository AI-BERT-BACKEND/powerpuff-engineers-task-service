package com.aibert.dosw.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing a time-block overlap between two tasks (R17 — conflict detection).
 * Returned by {@code GET /api/tasks/calendar/conflicts}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictResponse {

    private String taskAId;
    private String taskATitle;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime taskAStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime taskAEnd;

    private String taskBId;
    private String taskBTitle;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime taskBStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime taskBEnd;
}

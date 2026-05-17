package com.aibert.dosw.domain.ports.in;

import com.aibert.dosw.application.dto.response.DailySummaryResponse;

public interface GetDailySummaryUseCase {
    DailySummaryResponse getDailySummary(String studentId);
}

package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.response.DailySummaryResponse;
import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.domain.ports.out.TaskRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetDailySummaryUseCaseImplTest {

    @Mock
    private TaskRepositoryPort taskRepositoryPort;

    @InjectMocks
    private GetDailySummaryUseCaseImpl getDailySummaryUseCase;

    @Test
    void getDailySummary_WhenNoTasksToday_ShouldReturnZeroPercentage() {
        when(taskRepositoryPort.findByStudentIdWithFilters(eq("S1"), isNull(), any(), any(), any(), any()))
                .thenReturn(List.of());

        DailySummaryResponse result = getDailySummaryUseCase.getDailySummary("S1");

        assertEquals(0, result.getCompletionPercentage());
        assertEquals(0.0, result.getTotalScheduledHours());
        assertEquals(0, result.getCompletedCount());
        assertEquals(0, result.getPendingCount());
    }

    @Test
    void getDailySummary_WhenAllTasksCompleted_ShouldReturn100Percent() {
        Task t1 = Task.builder().id("1").studentId("S1").status(TaskStatus.COMPLETED)
                .estimatedDurationMinutes(60).build();
        Task t2 = Task.builder().id("2").studentId("S1").status(TaskStatus.COMPLETED)
                .estimatedDurationMinutes(120).build();

        when(taskRepositoryPort.findByStudentIdWithFilters(eq("S1"), isNull(), any(), any(), any(), any()))
                .thenReturn(List.of(t1, t2));

        DailySummaryResponse result = getDailySummaryUseCase.getDailySummary("S1");

        assertEquals(100, result.getCompletionPercentage());
        assertEquals(3.0, result.getTotalScheduledHours());
        assertEquals(2, result.getCompletedCount());
        assertEquals(0, result.getPendingCount());
    }

    @Test
    void getDailySummary_WhenMixedTasks_ShouldCalculateCorrectPercentage() {
        Task completed = Task.builder().id("1").studentId("S1").status(TaskStatus.COMPLETED)
                .estimatedDurationMinutes(90).build();
        Task pending = Task.builder().id("2").studentId("S1").status(TaskStatus.TODO)
                .estimatedDurationMinutes(30).build();
        Task inProgress = Task.builder().id("3").studentId("S1").status(TaskStatus.IN_PROGRESS)
                .estimatedDurationMinutes(60).build();

        when(taskRepositoryPort.findByStudentIdWithFilters(eq("S1"), isNull(), any(), any(), any(), any()))
                .thenReturn(List.of(completed, pending, inProgress));

        DailySummaryResponse result = getDailySummaryUseCase.getDailySummary("S1");

        assertEquals(33, result.getCompletionPercentage());
        assertEquals(3.0, result.getTotalScheduledHours());
        assertEquals(1, result.getCompletedCount());
        assertEquals(2, result.getPendingCount());
    }

    @Test
    void getDailySummary_WhenTasksHaveNoEstimatedDuration_ShouldNotCountNullMinutes() {
        Task t1 = Task.builder().id("1").studentId("S1").status(TaskStatus.COMPLETED)
                .estimatedDurationMinutes(null).build();
        Task t2 = Task.builder().id("2").studentId("S1").status(TaskStatus.TODO)
                .estimatedDurationMinutes(60).build();

        when(taskRepositoryPort.findByStudentIdWithFilters(eq("S1"), isNull(), any(), any(), any(), any()))
                .thenReturn(List.of(t1, t2));

        DailySummaryResponse result = getDailySummaryUseCase.getDailySummary("S1");

        assertEquals(50, result.getCompletionPercentage());
        assertEquals(1.0, result.getTotalScheduledHours());
    }
}

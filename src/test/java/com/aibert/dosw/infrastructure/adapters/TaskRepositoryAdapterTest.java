package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.TaskEntity;
import com.aibert.dosw.infrastructure.adapters.persistence.mapper.TaskEntityMapper;
import com.aibert.dosw.infrastructure.adapters.persistence.repository.TaskJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRepositoryAdapterTest {

    @Mock
    private TaskJpaRepository jpaRepository;

    @Mock
    private TaskEntityMapper mapper;

    @InjectMocks
    private TaskRepositoryAdapter adapter;

    private final LocalDateTime deadline = LocalDateTime.now().plusDays(3);

    private Task buildTask(String id) {
        return Task.builder()
                .id(id).studentId("S1").subjectId("MATH-101")
                .title("Task").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                .estimatedDurationMinutes(60).deadline(deadline).build();
    }

    private TaskEntity buildEntity(String id) {
        return TaskEntity.builder()
                .id(id).studentId("S1").subjectId("MATH-101")
                .title("Task").status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                .estimatedDurationMinutes(60).deadline(deadline).build();
    }

    @Test
    void save_WhenTaskHasNoId_ShouldGenerateAndPersist() {
        Task task = buildTask(null);
        task.setId(null);
        TaskEntity entity = buildEntity("generated-id");
        Task saved = buildTask("generated-id");

        when(mapper.toEntity(any())).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(saved);

        Task result = adapter.save(task);

        assertNotNull(result.getId());
        verify(jpaRepository).save(entity);
    }

    @Test
    void save_WhenTaskAlreadyHasId_ShouldPreserveId() {
        Task task = buildTask("existing-id");
        TaskEntity entity = buildEntity("existing-id");
        Task saved = buildTask("existing-id");

        when(mapper.toEntity(task)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(saved);

        Task result = adapter.save(task);

        assertEquals("existing-id", result.getId());
    }

    @Test
    void existsDuplicate_ShouldDelegateToJpaRepository() {
        when(jpaRepository.existsByStudentIdAndSubjectIdAndTitleIgnoreCase("S1", "MATH-101", "Task")).thenReturn(true);

        boolean result = adapter.existsDuplicate("S1", "MATH-101", "Task");

        assertTrue(result);
    }

    @Test
    void findByStudentId_ShouldReturnMappedTasks() {
        TaskEntity entity = buildEntity("task-1");
        Task task = buildTask("task-1");
        when(jpaRepository.findByStudentId("S1")).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(task);

        List<Task> result = adapter.findByStudentId("S1");

        assertEquals(1, result.size());
        assertEquals("task-1", result.get(0).getId());
    }

    @Test
    void findById_WhenExists_ShouldReturnMappedTask() {
        TaskEntity entity = buildEntity("task-1");
        Task task = buildTask("task-1");
        when(jpaRepository.findById("task-1")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(task);

        Optional<Task> result = adapter.findById("task-1");

        assertTrue(result.isPresent());
        assertEquals("task-1", result.get().getId());
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        when(jpaRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<Task> result = adapter.findById("missing");

        assertTrue(result.isEmpty());
    }

    @Test
    void saveAll_ShouldMapAndPersistAllTasks() {
        Task task = buildTask("task-1");
        TaskEntity entity = buildEntity("task-1");
        when(mapper.toEntity(task)).thenReturn(entity);
        when(jpaRepository.saveAll(anyList())).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(task);

        List<Task> result = adapter.saveAll(List.of(task));

        assertEquals(1, result.size());
        verify(jpaRepository).saveAll(anyList());
    }

    @Test
    void findByStudentIdWithFilters_WhenStatusFilter_ShouldFilterResults() {
        TaskEntity todoEntity = buildEntity("task-1");
        TaskEntity doneEntity = buildEntity("task-2");
        doneEntity.setStatus(TaskStatus.COMPLETED);

        Task todoTask = buildTask("task-1");

        when(jpaRepository.findByStudentId("S1")).thenReturn(List.of(todoEntity, doneEntity));
        when(mapper.toDomain(todoEntity)).thenReturn(todoTask);

        List<Task> result = adapter.findByStudentIdWithFilters("S1", TaskStatus.TODO, null, null, null, null);

        assertEquals(1, result.size());
        assertEquals(TaskStatus.TODO, result.get(0).getStatus());
    }

    @Test
    void save_WhenTaskIdIsBlank_ShouldGenerateNewId() {
        Task task = buildTask("   ");
        TaskEntity entity = buildEntity("new-id");
        Task saved = buildTask("new-id");

        when(mapper.toEntity(any())).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(saved);

        Task result = adapter.save(task);

        assertNotNull(result.getId());
        verify(jpaRepository).save(entity);
    }

    @Test
    void saveAll_WhenTaskHasNoId_ShouldGenerateId() {
        Task task = buildTask(null);
        task.setId(null);
        TaskEntity entity = buildEntity("generated");
        Task saved = buildTask("generated");

        when(mapper.toEntity(any())).thenReturn(entity);
        when(jpaRepository.saveAll(anyList())).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(saved);

        List<Task> result = adapter.saveAll(List.of(task));

        assertEquals(1, result.size());
        verify(jpaRepository).saveAll(anyList());
    }

    @Test
    void findByStudentIdWithFilters_WithDateRange_ShouldIncludeMatchingTasks() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(10);
        TaskEntity entity = buildEntity("task-1");
        Task task = buildTask("task-1");

        when(jpaRepository.findByStudentId("S1")).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(task);

        List<Task> result = adapter.findByStudentIdWithFilters("S1", null, start, end, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void findByStudentIdWithFilters_WhenDeadlineNullAndStartDateFilter_ShouldExclude() {
        TaskEntity entityNoDeadline = buildEntity("task-no-deadline");
        entityNoDeadline.setDeadline(null);

        when(jpaRepository.findByStudentId("S1")).thenReturn(List.of(entityNoDeadline));

        List<Task> result = adapter.findByStudentIdWithFilters("S1", null,
                LocalDateTime.now().minusDays(1), null, null, null);

        assertEquals(0, result.size());
    }

    @Test
    void findByStudentIdWithFilters_WhenDeadlineAfterEndDate_ShouldExclude() {
        TaskEntity entity = buildEntity("task-1"); // deadline = now+3, end = now+1
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        when(jpaRepository.findByStudentId("S1")).thenReturn(List.of(entity));

        List<Task> result = adapter.findByStudentIdWithFilters("S1", null, null, endDate, null, null);

        assertEquals(0, result.size());
    }

    @Test
    void saveAll_WhenTaskIdIsBlank_ShouldGenerateNewId() {
        Task task = buildTask("   ");
        TaskEntity entity = buildEntity("generated-id");
        Task saved = buildTask("generated-id");

        when(mapper.toEntity(any())).thenReturn(entity);
        when(jpaRepository.saveAll(anyList())).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(saved);

        List<Task> result = adapter.saveAll(List.of(task));

        assertEquals(1, result.size());
        verify(jpaRepository).saveAll(anyList());
    }

    @Test
    void findByStudentIdWithFilters_WhenDeadlineBeforeStartDate_ShouldExclude() {
        TaskEntity entityPastDeadline = buildEntity("past-task");
        entityPastDeadline.setDeadline(LocalDateTime.now().minusDays(5));

        when(jpaRepository.findByStudentId("S1")).thenReturn(List.of(entityPastDeadline));

        List<Task> result = adapter.findByStudentIdWithFilters("S1", null,
                LocalDateTime.now().minusDays(1), null, null, null);

        assertEquals(0, result.size());
    }

    @Test
    void findByStudentIdWithFilters_WhenDeadlineNullAndEndDateFilter_ShouldExclude() {
        TaskEntity entityNoDeadline = buildEntity("no-deadline");
        entityNoDeadline.setDeadline(null);

        when(jpaRepository.findByStudentId("S1")).thenReturn(List.of(entityNoDeadline));

        List<Task> result = adapter.findByStudentIdWithFilters("S1", null, null,
                LocalDateTime.now().plusDays(5), null, null);

        assertEquals(0, result.size());
    }

    @Test
    void findByStudentIdWithFilters_WhenSubjectIdFilter_ShouldReturnMatchingTasks() {
        TaskEntity mathEntity = buildEntity("math-task");
        TaskEntity physEntity = buildEntity("phys-task");
        physEntity.setSubjectId("PHYS-201");

        Task mathTask = buildTask("math-task"); // subjectId = "MATH-101"
        // physEntity won't be mapped because it's filtered out before mapper is called

        when(jpaRepository.findByStudentId("S1")).thenReturn(List.of(mathEntity, physEntity));
        when(mapper.toDomain(mathEntity)).thenReturn(mathTask);

        List<Task> result = adapter.findByStudentIdWithFilters("S1", null, null, null, "MATH-101", null);

        assertEquals(1, result.size());
        assertEquals("math-task", result.get(0).getId());
    }

    @Test
    void findByStudentIdWithFilters_WhenTaskTypeFilter_ShouldReturnMatchingTasks() {
        TaskEntity examEntity = buildEntity("exam-task");
        examEntity.setTaskType(com.aibert.dosw.domain.model.TaskType.EXAMEN);
        TaskEntity tareaEntity = buildEntity("tarea-task");
        tareaEntity.setTaskType(com.aibert.dosw.domain.model.TaskType.TAREA);

        Task examTask = buildTask("exam-task");

        when(jpaRepository.findByStudentId("S1")).thenReturn(List.of(examEntity, tareaEntity));
        when(mapper.toDomain(examEntity)).thenReturn(examTask);

        List<Task> result = adapter.findByStudentIdWithFilters("S1", null, null, null, null,
                com.aibert.dosw.domain.model.TaskType.EXAMEN);

        assertEquals(1, result.size());
        assertEquals("exam-task", result.get(0).getId());
    }

    @Test
    void deleteById_ShouldDelegateToJpaRepository() {
        doNothing().when(jpaRepository).deleteById("task-to-delete");

        adapter.deleteById("task-to-delete");

        verify(jpaRepository).deleteById("task-to-delete");
    }
}


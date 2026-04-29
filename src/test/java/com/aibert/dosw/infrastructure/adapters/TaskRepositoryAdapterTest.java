package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.domain.model.TaskPriority;
import com.aibert.dosw.domain.model.TaskStatus;
import com.aibert.dosw.infrastructure.external.TaskEntity;
import com.aibert.dosw.infrastructure.external.TaskJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRepositoryAdapterTest {

    @Mock
    private TaskJpaRepository taskJpaRepository;

    @Mock
    private TaskEntityMapper taskEntityMapper;

    @InjectMocks
    private TaskRepositoryAdapter taskRepositoryAdapter;

    @Test
    void save_ShouldMapAndSaveAndReturnModel() {
        // Arrange
        Task taskInput = Task.builder()
                .title("Domain Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        TaskEntity entityInput = TaskEntity.builder()
                .title("Domain Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        TaskEntity savedEntity = TaskEntity.builder()
                .id("uuid")
                .title("Domain Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        Task returnedTask = Task.builder()
                .id("uuid")
                .title("Domain Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        when(taskEntityMapper.toEntity(taskInput)).thenReturn(entityInput);
        when(taskJpaRepository.save(entityInput)).thenReturn(savedEntity);
        when(taskEntityMapper.toModel(savedEntity)).thenReturn(returnedTask);

        // Act
        Task result = taskRepositoryAdapter.save(taskInput);

        // Assert
        assertNotNull(result);
        assertEquals("uuid", result.getId());
        assertEquals(TaskStatus.TODO, result.getStatus());
        
        verify(taskEntityMapper).toEntity(taskInput);
        verify(taskJpaRepository).save(entityInput);
        verify(taskEntityMapper).toModel(savedEntity);
    }
}

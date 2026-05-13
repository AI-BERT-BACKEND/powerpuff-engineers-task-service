package com.aibert.dosw.application.mapper;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.domain.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between task DTOs and the {@link Task} domain model.
 * Managed as a Spring bean via {@code componentModel = "spring"}.
 */
@Mapper(componentModel = "spring")
public abstract class TaskDtoMapper {

    /**
     * Converts a {@link CreateTaskRequest} to a {@link Task} domain model.
     * The fields {@code id}, {@code status}, {@code scheduledDate}, and {@code completedAt}
     * are intentionally left unset because they are assigned later by the use case layer.
     *
     * @param request   the incoming creation request with validated task data
     * @param studentId the student identifier obtained from the {@code X-User-Id} header
     * @return a {@link Task} ready for persistence
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "scheduledDate", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    public abstract Task toModel(CreateTaskRequest request, String studentId);

    /**
     * Converts a {@link Task} domain model to a {@link TaskResponse} DTO.
     *
     * @param task the task domain object to convert
     * @return the corresponding API response DTO
     */
    public abstract TaskResponse toResponse(Task task);
}


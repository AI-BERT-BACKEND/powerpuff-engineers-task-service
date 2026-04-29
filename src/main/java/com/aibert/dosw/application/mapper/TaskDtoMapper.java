package com.aibert.dosw.application.mapper;

import com.aibert.dosw.application.dto.request.CreateTaskRequest;
import com.aibert.dosw.application.dto.response.TaskResponse;
import com.aibert.dosw.domain.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskDtoMapper {
    Task toModel(CreateTaskRequest request);
    TaskResponse toResponse(Task task);
}

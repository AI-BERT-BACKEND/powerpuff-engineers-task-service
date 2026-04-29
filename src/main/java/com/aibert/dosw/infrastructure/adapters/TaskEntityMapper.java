package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.domain.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.aibert.dosw.infrastructure.external.TaskEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskEntityMapper {
    TaskEntity toEntity(Task task);
    Task toModel(TaskEntity entity);
}

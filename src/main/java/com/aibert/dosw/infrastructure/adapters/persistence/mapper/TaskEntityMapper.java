package com.aibert.dosw.infrastructure.adapters.persistence.mapper;

import com.aibert.dosw.domain.model.Task;
import com.aibert.dosw.infrastructure.adapters.persistence.entity.TaskEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between the {@link Task} domain model
 * and the {@link TaskEntity} JPA entity.
 * Managed as a Spring bean via {@code componentModel = "spring"}.
 */
@Mapper(componentModel = "spring")
public interface TaskEntityMapper {

    /**
     * Converts a {@link Task} domain model to a {@link TaskEntity} suitable for persistence.
     *
     * @param task the domain model to convert
     * @return the corresponding JPA entity
     */
    TaskEntity toEntity(Task task);

    /**
     * Converts a {@link TaskEntity} from the database to a {@link Task} domain model.
     *
     * @param entity the JPA entity to convert
     * @return the corresponding domain model
     */
    Task toDomain(TaskEntity entity);
}


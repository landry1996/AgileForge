package com.agileforge.application.mapper;

import com.agileforge.application.dto.response.UserResponse;
import com.agileforge.domain.model.User;
import com.agileforge.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "deleted", ignore = true)
    UserEntity toEntity(User user);

    User toDomain(UserEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(User user, @MappingTarget UserEntity entity);
}

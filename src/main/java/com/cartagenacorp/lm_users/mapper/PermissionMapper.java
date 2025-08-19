package com.cartagenacorp.lm_users.mapper;

import com.cartagenacorp.lm_users.dto.PermissionDto;
import com.cartagenacorp.lm_users.entity.Permission;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PermissionMapper {
    Permission toEntity(PermissionDto permissionDto);

    PermissionDto toDto(Permission permission);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Permission partialUpdate(PermissionDto permissionDto, @MappingTarget Permission permission);
}
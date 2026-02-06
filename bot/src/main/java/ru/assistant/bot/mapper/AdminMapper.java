package ru.assistant.bot.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.assistant.bot.model.Admin;
import ru.assistant.bot.model.dto.request.AdminCreateRequest;
import ru.assistant.bot.model.dto.request.AdminUpdateRequest;
import ru.assistant.bot.model.dto.response.AdminResponse;

/**
 * AdminMapper
 * @author agent
 * @since 03.02.2026
 */
@Mapper(componentModel = "spring")
public interface AdminMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reviewedSubmissions", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Admin toEntity(AdminCreateRequest request);

    AdminResponse toResponse(Admin admin);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "telegramId", ignore = true)
    @Mapping(target = "reviewedSubmissions", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Admin admin, AdminUpdateRequest request);
}
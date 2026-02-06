package ru.assistant.bot.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.assistant.bot.model.Student;
import ru.assistant.bot.model.dto.request.StudentRegistrationRequest;
import ru.assistant.bot.model.dto.request.StudentUpdateRequest;
import ru.assistant.bot.model.dto.response.StudentResponse;

/**
 * StudentMapper
 * @author agent
 * @since 03.02.2026
 */
@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "lastActivity", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "progresses", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "email", ignore = true)
    Student toEntity(StudentRegistrationRequest request);

    StudentResponse toResponse(Student student);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "telegramId", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "lastActivity", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "progresses", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    void updateEntity(@MappingTarget Student student, StudentUpdateRequest request);
}
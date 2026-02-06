package ru.assistant.bot.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.assistant.bot.model.Course;
import ru.assistant.bot.model.dto.request.CourseCreateRequest;
import ru.assistant.bot.model.dto.request.CourseUpdateRequest;
import ru.assistant.bot.model.dto.response.CourseResponse;

/**
 * CourseMapper
 * @author agent
 * @since 03.02.2026
 */
@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "studentProgresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Course toEntity(CourseCreateRequest request);

    @Mapping(target = "totalAssignments", expression = "java(course.getTotalAssignments())")
    @Mapping(target = "activeStudents", ignore = true)
    CourseResponse toResponse(Course course);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "studentProgresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Course course, CourseUpdateRequest request);
}
package ru.assistant.bot.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.assistant.bot.model.Assignment;
import ru.assistant.bot.model.Course;
import ru.assistant.bot.model.dto.request.AssignmentCreateRequest;
import ru.assistant.bot.model.dto.request.AssignmentUpdateRequest;
import ru.assistant.bot.model.dto.response.AssignmentResponse;
import ru.assistant.bot.model.dto.AssignmentWithCourseDto;

/**
 * AssignmentMapper
 * @author agent
 * @since 03.02.2026
 */
@Mapper(componentModel = "spring", uses = {CourseMapper.class})
public interface AssignmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Assignment toEntity(AssignmentCreateRequest request);

    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.name", target = "courseName")
    @Mapping(target = "isPastDeadline", expression = "java(assignment.isPastDeadline())")
    @Mapping(target = "totalSubmissions", ignore = true)
    @Mapping(target = "averageScore", ignore = true)
    AssignmentResponse toResponse(Assignment assignment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Assignment assignment, AssignmentUpdateRequest request);

    @Mapping(target = "course", source = "course")
    AssignmentWithCourseDto toDtoWithCourse(Assignment assignment);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "icon", source = "icon")
    AssignmentWithCourseDto.CourseDto toCourseDto(Course course);
}
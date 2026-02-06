package ru.assistant.bot.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.assistant.bot.model.StudentProgress;
import ru.assistant.bot.model.dto.response.StudentProgressResponse;

/**
 * StudentProgressMapper
 * @author agent
 * @since 03.02.2026
 */
@Mapper(componentModel = "spring", uses = {StudentMapper.class, CourseMapper.class})
public interface StudentProgressMapper {

    @Mapping(target = "grade", expression = "java(progress.getGrade())")
    StudentProgressResponse toResponse(StudentProgress progress);
}
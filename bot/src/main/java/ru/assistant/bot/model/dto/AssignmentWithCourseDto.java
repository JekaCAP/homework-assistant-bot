package ru.assistant.bot.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AssignmentWithCourseDto
 * @author agent
 * @since 04.02.2026
 */
@Data
public class AssignmentWithCourseDto {
    private Long id;
    private String title;
    private String description;
    private Integer number;
    private Integer maxScore;
    private LocalDateTime deadline;
    private CourseDto course;

    @Data
    public static class CourseDto {
        private Long id;
        private String name;
        private String icon;
    }
}
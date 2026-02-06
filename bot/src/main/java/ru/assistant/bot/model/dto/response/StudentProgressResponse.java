package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * StudentProgressResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressResponse {
    private Long id;
    private StudentResponse student;
    private CourseResponse course;
    private Integer assignmentsSubmitted;
    private Integer assignmentsAccepted;
    private Integer assignmentsRejected;
    private Integer totalScore;
    private BigDecimal averageScore;
    private BigDecimal completionPercentage;
    private Integer rank;
    private String grade;
    private LocalDateTime lastSubmissionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package ru.assistant.bot.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignmentStatsDto {
    private Long assignmentId;
    private String title;
    private Integer number;
    private Long totalSubmissions;
    private Long acceptedSubmissions;
    private Long pendingSubmissions;
    private Double averageScore;
    private Integer maxScore;
    private Double completionRate;
}
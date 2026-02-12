package ru.assistant.bot.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * StudentRatingDto
 *
 * @author agent
 * @since 10.02.2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRatingDto {
    private Long studentId;
    private String fullName;
    private String telegramUsername;
    private String githubUsername;
    private Integer assignmentsSubmitted;
    private Integer assignmentsAccepted;
    private BigDecimal averageScore;
    private Integer totalScore;
    private Integer rank;

    public String getFormattedRank() {
        return rank != null ? "#" + rank : "—";
    }

    public String getFormattedAverageScore() {
        if (averageScore == null || averageScore.compareTo(BigDecimal.ZERO) == 0) {
            return "—";
        }
        return String.format("%.1f", averageScore);
    }

    public String getCompletionRate() {
        if (assignmentsSubmitted == null || assignmentsSubmitted == 0) {
            return "0%";
        }
        double rate = (assignmentsAccepted * 100.0) / assignmentsSubmitted;
        return String.format("%.0f%%", rate);
    }

    public String getShortName() {
        if (fullName == null) return "—";
        if (fullName.length() <= 15) return fullName;
        return fullName.substring(0, 12) + "...";
    }
}
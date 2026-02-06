package ru.assistant.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * StudentProgress
 * @author agent
 * @since 03.02.2026
 */
@Entity
@Table(name = "student_progress",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_progress_student_course", columnNames = {"student_id", "course_id"})
        },
        indexes = {
                @Index(name = "idx_progress_average_score", columnList = "average_score DESC"),
                @Index(name = "idx_progress_student_id", columnList = "student_id"),
                @Index(name = "idx_progress_course_id", columnList = "course_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "progress_seq")
    @SequenceGenerator(name = "progress_seq", sequenceName = "progress_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_progress_student"))
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false, foreignKey = @ForeignKey(name = "fk_progress_course"))
    private Course course;

    @Column(name = "assignments_submitted")
    @Builder.Default
    private Integer assignmentsSubmitted = 0;

    @Column(name = "assignments_accepted")
    @Builder.Default
    private Integer assignmentsAccepted = 0;

    @Column(name = "assignments_rejected")
    @Builder.Default
    private Integer assignmentsRejected = 0;

    @Column(name = "total_score")
    @Builder.Default
    private Integer totalScore = 0;

    @Column(name = "average_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal averageScore = BigDecimal.ZERO;

    @Column(name = "completion_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal completionPercentage = BigDecimal.ZERO;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Column(name = "last_submission_date")
    private LocalDateTime lastSubmissionDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business methods
    public void updateStatistics(int totalAssignmentsInCourse) {
        if (assignmentsSubmitted > 0) {
            // Calculate average score
            if (totalScore > 0 && assignmentsSubmitted > 0) {
                this.averageScore = BigDecimal.valueOf(totalScore)
                        .divide(BigDecimal.valueOf(assignmentsSubmitted), 2, RoundingMode.HALF_UP);
            }

            // Calculate completion percentage
            if (totalAssignmentsInCourse > 0) {
                this.completionPercentage = BigDecimal.valueOf(assignmentsAccepted * 100.0)
                        .divide(BigDecimal.valueOf(totalAssignmentsInCourse), 2, RoundingMode.HALF_UP);
            }
        }
    }

    public String getGrade() {
        if (averageScore == null || averageScore.equals(BigDecimal.ZERO)) {
            return "N/A";
        }

        double score = averageScore.doubleValue();
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
}
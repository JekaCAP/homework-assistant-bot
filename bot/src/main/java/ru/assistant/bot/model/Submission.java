package ru.assistant.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.assistant.bot.model.enums.SubmissionStatus;

import java.time.LocalDateTime;

/**
 * Submission
 * @author agent
 * @since 03.02.2026
 */
@Entity
@Table(name = "submissions",
        indexes = {
                @Index(name = "idx_submission_status", columnList = "status"),
                @Index(name = "idx_submission_student", columnList = "student_id"),
                @Index(name = "idx_submission_assignment", columnList = "assignment_id"),
                @Index(name = "idx_submission_created", columnList = "submitted_at DESC")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "submission_seq")
    @SequenceGenerator(name = "submission_seq", sequenceName = "submission_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_submission_student"))
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_submission_assignment"))
    private Assignment assignment;

    @Column(name = "pr_url", nullable = false, length = 500)
    private String prUrl;

    @Column(name = "pr_number")
    private Integer prNumber;

    @Column(name = "github_repo", length = 300)
    private String githubRepo;

    @Column(name = "commit_hash", length = 100)
    private String commitHash;

    @Column(name = "branch_name", length = 100)
    private String branchName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(name = "score")
    private Integer score;

    @Column(name = "reviewer_comment", columnDefinition = "TEXT")
    private String reviewerComment;

    @Column(name = "student_comment", columnDefinition = "TEXT")
    private String studentComment;

    @Column(name = "auto_checks_passed")
    private Boolean autoChecksPassed;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "resubmitted_at")
    private LocalDateTime resubmittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", foreignKey = @ForeignKey(name = "fk_submission_reviewer"))
    private Admin reviewer;

    @Version
    private Long version;

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public void markAsReviewed() {
        this.reviewedAt = LocalDateTime.now();
        this.status = (this.score >= this.assignment.getMaxScore() * 0.6)
                ? SubmissionStatus.ACCEPTED
                : SubmissionStatus.UNDER_REVIEW;
    }

    public boolean isUnderReview() {
        return status == SubmissionStatus.UNDER_REVIEW;
    }

    public boolean isAccepted() {
        return status == SubmissionStatus.ACCEPTED;
    }

    public boolean isRejected() {
        return status == SubmissionStatus.REJECTED;
    }

    public boolean needsRevision() {
        return status == SubmissionStatus.NEEDS_REVISION;
    }

}
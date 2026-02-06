package ru.assistant.bot.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import ru.assistant.bot.model.enums.AssignmentType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Assignment
 * @author agent
 * @since 03.02.2026
 */
@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assignment_seq")
    @SequenceGenerator(name = "assignment_seq", sequenceName = "assignment_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "requirements", columnDefinition = "jsonb")
    private Set<String> requirements;

    @Column(name = "max_score")
    @Builder.Default
    private Integer maxScore = 100;

    @Column(name = "min_score")
    @Builder.Default
    private Integer minScore = 0;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "github_template_url", length = 500)
    private String githubTemplateUrl;

    @Column(name = "test_command", length = 200)
    private String testCommand;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    @Builder.Default
    private AssignmentType type = AssignmentType.HOMEWORK;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Submission> submissions = new HashSet<>();

    public boolean isPastDeadline() {
        return deadline != null && LocalDateTime.now().isAfter(deadline);
    }

    public boolean isAcceptingSubmissions() {
        return Boolean.TRUE.equals(isActive) && !isPastDeadline();
    }

    public String getFullTitle() {
        return String.format("%s #%d: %s", course.getName(), number, title);
    }
}
package ru.assistant.bot.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.assistant.bot.model.Assignment;
import ru.assistant.bot.model.Student;
import ru.assistant.bot.model.Submission;
import ru.assistant.bot.model.enums.SubmissionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SubmissionRepository
 * @author agent
 * @since 03.02.2026
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /**
     * Подсчет по статусу и диапазону дат
     */
    long countByStatusAndReviewedAtBetween(SubmissionStatus status, LocalDateTime start, LocalDateTime end);

    /**
     * Последние N сдач
     */
    @Query("SELECT s FROM Submission s " +
           "JOIN FETCH s.student " +
           "JOIN FETCH s.assignment a " +
           "JOIN FETCH a.course " +
           "ORDER BY s.submittedAt DESC")
    List<Submission> findTopNOrderBySubmittedAtDesc(Pageable pageable);

    default List<Submission> findTopNOrderBySubmittedAtDesc(int limit) {
        return findTopNOrderBySubmittedAtDesc(PageRequest.of(0, limit));
    }

    @Query("SELECT s FROM Submission s " +
           "WHERE s.student.id = :studentId AND s.assignment.id = :assignmentId " +
           "ORDER BY s.submittedAt DESC")
    Optional<Submission> findTopByStudentIdAndAssignmentIdOrderBySubmittedAtDesc(Long studentId, Long assignmentId);

    List<Submission> findByStatusOrderBySubmittedAtDesc(SubmissionStatus status);

    long countByStatus(SubmissionStatus status);

    @Query("SELECT s FROM Submission s " +
           "JOIN FETCH s.assignment a " +
           "JOIN FETCH a.course c " +
           "JOIN FETCH s.student st " +
           "WHERE s.id = :id")
    Optional<Submission> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT s FROM Submission s WHERE s.student.telegramId = :telegramId")
    List<Submission> findByStudentTelegramId(@Param("telegramId") Long telegramId);

    @Query("SELECT s.student.id, COUNT(s) FROM Submission s " +
           "WHERE s.status = 'ACCEPTED' AND s.student.id IN :studentIds " +
           "GROUP BY s.student.id")
    List<Object[]> countAcceptedSubmissionsByStudentIds(@Param("studentIds") List<Long> studentIds);

    default Map<Long, Long> getAcceptedSubmissionsCountMap(List<Long> studentIds) {
        List<Object[]> results = countAcceptedSubmissionsByStudentIdsRaw(studentIds);
        return results.stream()
                .collect(Collectors.toMap(
                        obj -> (Long) obj[0],
                        obj -> (Long) obj[1]
                ));
    }

    @Query("SELECT s.student.id, COUNT(s) FROM Submission s " +
           "WHERE s.status = 'ACCEPTED' AND s.student.id IN :studentIds " +
           "GROUP BY s.student.id")
    List<Object[]> countAcceptedSubmissionsByStudentIdsRaw(@Param("studentIds") List<Long> studentIds);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.student.id = :studentId")
    Long countByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.status = 'REJECTED'")
    Long countRejectedSubmissions();

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.status = 'NEEDS_REVISION'")
    Long countNeedsRevisionSubmissions();

    long countByAssignmentId(Long assignmentId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment.id = :assignmentId AND s.status = 'ACCEPTED'")
    long countAcceptedByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment.id = :assignmentId AND s.status = 'PENDING'")
    long countPendingByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.assignment.id = :assignmentId AND s.score IS NOT NULL")
    Double getAverageScoreByAssignmentId(@Param("assignmentId") Long assignmentId);
}
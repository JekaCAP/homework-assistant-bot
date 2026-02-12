package ru.assistant.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.assistant.bot.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * StudentRepository
 * @author agent
 * @since 03.02.2026
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByTelegramId(Long telegramId);

    Optional<Student> findByGithubUsername(String githubUsername);

    List<Student> findByIsActiveTrue();

    @Query("SELECT COUNT(s) FROM Student s WHERE s.isActive = true")
    long countActiveStudents();

    @Query("SELECT s FROM Student s WHERE s.githubUsername IS NOT NULL")
    List<Student> findStudentsWithGitHub();

    @Query("SELECT COUNT(s) FROM Student s WHERE s.isActive = true")
    Long countByIsActiveTrue();

    @Query("SELECT s FROM Student s WHERE s.isActive = true ORDER BY s.registrationDate DESC LIMIT :limit")
    List<Student> findTopByIsActiveTrueOrderByRegistrationDateDesc(int limit);

    @Query("SELECT s FROM Student s WHERE s.isActive = true ORDER BY s.lastActivity DESC LIMIT :limit")
    List<Student> findTopActiveByLastActivity(int limit);

    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.student.id = :studentId AND s.status = 'ACCEPTED'")
    Double calculateAverageScore(Long studentId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.student.id = :studentId AND s.status = 'ACCEPTED'")
    int countAcceptedSubmissions(Long studentId);

    @Query(value = """
            SELECT s.id, s.full_name, s.telegram_username, s.github_username,
                   COUNT(sub.id) as submissions_count,
                   COUNT(CASE WHEN sub.status = 'ACCEPTED' THEN 1 END) as accepted_count,
                   AVG(sub.score) as avg_score,
                   COALESCE(SUM(sub.score), 0) as total_score
            FROM students s
            LEFT JOIN submissions sub ON s.id = sub.student_id 
                AND sub.status IN ('ACCEPTED')
            WHERE s.is_active = true
            GROUP BY s.id, s.full_name, s.telegram_username, s.github_username
            HAVING COUNT(sub.id) > 0
            ORDER BY avg_score DESC NULLS LAST, total_score DESC, accepted_count DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopStudentsByAverageScore(int limit);

    @Query(value = """
        SELECT s.id, s.full_name, s.telegram_username, s.github_username,
               COUNT(sub.id) as submissions_count,
               COUNT(CASE WHEN sub.status = 'ACCEPTED' THEN 1 END) as accepted_count,
               AVG(sub.score) as avg_score,
               COALESCE(SUM(sub.score), 0) as total_score
        FROM students s
        LEFT JOIN submissions sub ON s.id = sub.student_id
            AND sub.status IN ('ACCEPTED')
        WHERE s.is_active = true
        GROUP BY s.id, s.full_name, s.telegram_username, s.github_username
        HAVING COUNT(sub.id) > 0
        ORDER BY accepted_count DESC, avg_score DESC, total_score DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopStudentsByAcceptedSubmissions(int limit);

    @Query(value = """
        SELECT s.id, s.full_name, s.telegram_username, s.github_username,
               COUNT(sub.id) as submissions_count,
               COUNT(CASE WHEN sub.status = 'ACCEPTED' THEN 1 END) as accepted_count,
               AVG(sub.score) as avg_score
        FROM students s
        JOIN submissions sub ON s.id = sub.student_id
        JOIN assignments a ON sub.assignment_id = a.id
        WHERE s.is_active = true 
          AND a.course_id = :courseId
          AND sub.status IN ('ACCEPTED')
        GROUP BY s.id, s.full_name, s.telegram_username, s.github_username
        HAVING COUNT(sub.id) > 0
        ORDER BY avg_score DESC NULLS LAST, accepted_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopStudentsByCourse(Long courseId, int limit);

    @Query(value = """
        WITH ranked_students AS (
            SELECT s.id,
                   ROW_NUMBER() OVER (
                       ORDER BY 
                           COALESCE(AVG(sub.score), 0) DESC,
                           COUNT(CASE WHEN sub.status = 'ACCEPTED' THEN 1 END) DESC
                   ) as rank_position
            FROM students s
            LEFT JOIN submissions sub ON s.id = sub.student_id 
                AND sub.status IN ('ACCEPTED')
            WHERE s.is_active = true
            GROUP BY s.id
        )
        SELECT rank_position FROM ranked_students
        WHERE id = :studentId
        """, nativeQuery = true)
    Optional<Integer> calculateStudentRank(Long studentId);
}
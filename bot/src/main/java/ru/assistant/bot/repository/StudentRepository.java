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
}
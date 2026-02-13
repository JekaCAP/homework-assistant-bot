package ru.assistant.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.assistant.bot.model.Course;

import java.util.List;
import java.util.Optional;

/**
 * CourseRepository
 * @author agent
 * @since 03.02.2026
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    List<Course> findByIsActiveTrueOrderBySortOrder();

    @Query("SELECT COUNT(DISTINCT s.student) FROM Submission s " +
           "JOIN s.assignment a " +
           "WHERE a.course.id = :courseId AND s.status = 'ACCEPTED'")
    long countActiveStudentsInCourse(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(a) FROM Course c JOIN c.assignments a " +
           "WHERE c.id = :courseId AND a.isActive = true")
    long countAssignmentsByCourseId(@Param("courseId") Long courseId);

    long countByIsActiveTrue();

    @Query("SELECT COUNT(DISTINCT s.student.id) FROM Submission s " +
           "WHERE s.assignment.course.id = :courseId AND s.status = 'ACCEPTED'")
    long countActiveStudents(@Param("courseId") Long courseId);
}
package ru.assistant.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.assistant.bot.model.Assignment;
import ru.assistant.bot.model.Course;

import java.util.List;
import java.util.Optional;

/**
 * AssignmentRepository
 * @author agent
 * @since 03.02.2026
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByCourseAndIsActiveTrueOrderByNumber(Course course);

    Optional<Assignment> findByCourseAndNumber(Course course, Integer number);

    @Query("SELECT a FROM Assignment a WHERE a.course.id = :courseId AND a.isActive = true " +
           "ORDER BY a.number")
    List<Assignment> findActiveAssignmentsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT a FROM Assignment a LEFT JOIN FETCH a.course WHERE a.id = :id")
    Optional<Assignment> findByIdWithCourse(@Param("id") Long id);
}
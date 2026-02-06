package ru.assistant.bot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.assistant.bot.model.Course;
import ru.assistant.bot.model.Student;
import ru.assistant.bot.model.StudentProgress;

import java.util.List;
import java.util.Optional;

/**
 * StudentProgressRepository
 * @author agent
 * @since 03.02.2026
 */
@Repository
public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {

    @Query("SELECT sp FROM StudentProgress sp WHERE sp.course.id = :courseId " +
           "ORDER BY sp.averageScore DESC NULLS LAST")
    List<StudentProgress> findRankingsByCourse(@Param("courseId") Long courseId);

    @Query("SELECT sp FROM StudentProgress sp WHERE sp.student.id = :studentId " +
           "AND sp.completionPercentage >= 100")
    List<StudentProgress> findCompletedCourses(@Param("studentId") Long studentId);

    @Query("SELECT sp FROM StudentProgress sp WHERE sp.course.id = :courseId " +
           "AND sp.assignmentsSubmitted > 0 " +
           "ORDER BY sp.averageScore DESC")
    Page<StudentProgress> findTopPerformers(@Param("courseId") Long courseId,
                                            Pageable pageable);

    @Query("SELECT COUNT(sp) FROM StudentProgress sp WHERE sp.course.id = :courseId")
    long countByCourse(@Param("courseId") Long courseId);

    @Query("SELECT AVG(sp.averageScore) FROM StudentProgress sp WHERE sp.course.id = :courseId")
    Double findAverageCourseScore(@Param("courseId") Long courseId);

    @Query("SELECT sp FROM StudentProgress sp WHERE sp.course.id = :courseId " +
           "AND sp.student.isActive = true " +
           "AND sp.assignmentsSubmitted > 0 " +
           "ORDER BY sp.lastSubmissionDate DESC")
    List<StudentProgress> findRecentActivity(@Param("courseId") Long courseId);
}
package ru.assistant.bot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Базовые методы
    List<Assignment> findByCourseOrderByNumber(Course course);

    List<Assignment> findByCourseAndIsActiveTrueOrderByNumber(Course course);

    Optional<Assignment> findByCourseAndNumber(Course course, Integer number);

    // Активные задания
    @Query("SELECT a FROM Assignment a WHERE a.course.id = :courseId AND a.isActive = true " +
           "ORDER BY a.number")
    List<Assignment> findActiveAssignmentsByCourseId(@Param("courseId") Long courseId);

    // Все задания курса
    @Query("SELECT a FROM Assignment a WHERE a.course.id = :courseId " +
           "ORDER BY a.number")
    List<Assignment> findAllByCourseId(@Param("courseId") Long courseId);

    // Подсчеты
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.course.id = :courseId AND a.isActive = true")
    long countActiveByCourseId(@Param("courseId") Long courseId);

    // Загрузка с курсом
    @Query("SELECT a FROM Assignment a LEFT JOIN FETCH a.course WHERE a.id = :id")
    Optional<Assignment> findByIdWithCourse(@Param("id") Long id);

    // Поиск
    @Query("SELECT a FROM Assignment a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Assignment> search(@Param("query") String query);

    // Пагинация
    Page<Assignment> findByCourseId(Long courseId, Pageable pageable);

    // Статистика
    @Query("SELECT AVG(a.maxScore) FROM Assignment a WHERE a.course.id = :courseId")
    Double getAverageMaxScoreByCourse(@Param("courseId") Long courseId);

    @Query("SELECT SUM(a.estimatedHours) FROM Assignment a WHERE a.course.id = :courseId")
    Integer getTotalEstimatedHoursByCourse(@Param("courseId") Long courseId);

    // Деактивация просроченных
    @Query("SELECT a FROM Assignment a WHERE a.deadline < CURRENT_TIMESTAMP AND a.isActive = true")
    List<Assignment> findExpiredActiveAssignments();
}
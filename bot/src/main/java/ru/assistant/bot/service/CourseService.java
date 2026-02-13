package ru.assistant.bot.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.assistant.bot.mapper.CourseMapper;
import ru.assistant.bot.model.Course;
import ru.assistant.bot.model.dto.request.CourseCreateRequest;
import ru.assistant.bot.model.dto.request.CourseUpdateRequest;
import ru.assistant.bot.model.dto.response.CourseResponse;
import ru.assistant.bot.model.dto.response.CourseStats;
import ru.assistant.bot.model.enums.DifficultyLevel;
import ru.assistant.bot.repository.AssignmentRepository;
import ru.assistant.bot.repository.CourseRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CourseService
 * @author agent
 * @since 03.02.2026
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final CourseMapper courseMapper;

    /**
     * Получить CourseResponse по ID
     */
    public CourseResponse getCourseResponse(Long id) {
        Course course = findById(id)
                .orElseThrow(() -> new NotFoundException("Курс не найден: " + id));
        return courseMapper.toResponse(course);
    }

    /**
     * Получить все курсы для выпадающего списка
     */
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Course createCourse(CourseCreateRequest request) {
        // Проверка уникальности кода
        if (courseRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Курс с кодом " + request.getCode() + " уже существует");
        }

        Course course = Course.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon() != null ? request.getIcon() : "📘")
                .difficultyLevel(request.getDifficultyLevel() != null ?
                        DifficultyLevel.valueOf(request.getDifficultyLevel()) : DifficultyLevel.BEGINNER)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(true)
                .build();

        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, CourseUpdateRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        if (request.getName() != null) {
            course.setName(request.getName());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getIcon() != null) {
            course.setIcon(request.getIcon());
        }
        if (request.getDifficultyLevel() != null) {
            course.setDifficultyLevel(DifficultyLevel.valueOf(request.getDifficultyLevel()));
        }
        if (request.getSortOrder() != null) {
            course.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            course.setIsActive(request.getIsActive());
        }

        return courseRepository.save(course);
    }

    @Transactional
    public boolean toggleCourseActive(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        course.setIsActive(!course.getIsActive());
        courseRepository.save(course);
        return course.getIsActive();
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        // Проверка наличия заданий
        long assignmentsCount = courseRepository.countAssignmentsByCourseId(id);
        if (assignmentsCount > 0) {
            throw new RuntimeException("Нельзя удалить курс с заданиями. Сначала удалите все задания.");
        }

        courseRepository.delete(course);
    }

    public List<CourseResponse> getAllCoursesWithStats() {
        return courseRepository.findAll().stream()
                .map(course -> {
                    long assignmentsCount = courseRepository.countAssignmentsByCourseId(course.getId());
                    long activeStudentsCount = courseRepository.countActiveStudentsInCourse(course.getId());

                    return CourseResponse.builder()
                            .id(course.getId())
                            .code(course.getCode())
                            .name(course.getName())
                            .description(course.getDescription())
                            .icon(course.getIcon())
                            .difficultyLevel(course.getDifficultyLevel() != null ?
                                    course.getDifficultyLevel().name() : "BEGINNER")
                            .isActive(course.getIsActive())
                            .sortOrder(course.getSortOrder())
                            .totalAssignments((int) assignmentsCount)
                            .activeStudents((int) activeStudentsCount)
                            .createdAt(course.getCreatedAt())
                            .updatedAt(course.getUpdatedAt())
                            .build();
                })
                .sorted((c1, c2) -> {
                    if (c1.getSortOrder() == null) return 1;
                    if (c2.getSortOrder() == null) return -1;
                    return c1.getSortOrder().compareTo(c2.getSortOrder());
                })
                .toList();
    }

    public CourseStats getCourseStats(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        CourseResponse courseResponse = CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .description(course.getDescription())
                .icon(course.getIcon())
                .difficultyLevel(course.getDifficultyLevel() != null ?
                        course.getDifficultyLevel().name() : "BEGINNER")
                .isActive(course.getIsActive())
                .sortOrder(course.getSortOrder())
                .totalAssignments((int) courseRepository.countAssignmentsByCourseId(id))
                .activeStudents((int) courseRepository.countActiveStudentsInCourse(id))
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();

        // TODO: Добавить реальную статистику
        return CourseStats.builder()
                .course(courseResponse)
                .totalStudents(0L)
                .activeStudents(0L)
                .totalSubmissions(0L)
                .pendingSubmissions(0L)
                .averageScore(0.0)
                .completionRate(BigDecimal.ZERO)
                .build();
    }

    public long countAssignmentsByCourseId(Long courseId) {
        return courseRepository.countAssignmentsByCourseId(courseId);
    }

    public List<Course> getActiveCourses() {
        return courseRepository.findByIsActiveTrueOrderBySortOrder();
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    public Optional<Course> findByCode(String code) {
        return courseRepository.findByCode(code);
    }

    public long getActiveCoursesCount() {
        return courseRepository.countByIsActiveTrue();
    }
}
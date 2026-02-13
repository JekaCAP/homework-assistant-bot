package ru.assistant.bot.service;

import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.assistant.bot.mapper.AssignmentMapper;
import ru.assistant.bot.model.Assignment;
import ru.assistant.bot.model.Course;
import ru.assistant.bot.model.dto.AssignmentStatsDto;
import ru.assistant.bot.model.dto.request.AssignmentCreateRequest;
import ru.assistant.bot.model.dto.request.AssignmentUpdateRequest;
import ru.assistant.bot.model.dto.response.AssignmentResponse;
import ru.assistant.bot.repository.AssignmentRepository;
import ru.assistant.bot.model.dto.AssignmentWithCourseDto;
import ru.assistant.bot.repository.CourseRepository;
import ru.assistant.bot.repository.SubmissionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AssignmentService
 * @author agent
 * @since 03.02.2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentMapper assignmentMapper;

    public List<Assignment> getActiveAssignmentsByCourseId(Long courseId) {
        return assignmentRepository.findActiveAssignmentsByCourseId(courseId);
    }

    public Optional<Assignment> findById(Long id) {
        return assignmentRepository.findById(id);
    }

    public Optional<AssignmentWithCourseDto> findByIdWithCourse(Long id) {
        return assignmentRepository.findByIdWithCourse(id)
                .map(assignmentMapper::toDtoWithCourse);
    }

    public List<AssignmentResponse> getAll() {
        log.debug("Получение всех заданий");
        return assignmentRepository.findAll().stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получить задания курса
     */
    public List<AssignmentResponse> getByCourse(Long courseId) {
        log.debug("Получение заданий курса: {}", courseId);
        Course course = getCourseById(courseId);
        return assignmentRepository.findByCourseOrderByNumber(course).stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получить активные задания курса
     */
    public List<AssignmentResponse> getActiveByCourse(Long courseId) {
        log.debug("Получение активных заданий курса: {}", courseId);
        Course course = getCourseById(courseId);
        return assignmentRepository.findByCourseAndIsActiveTrueOrderByNumber(course).stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получить задание по ID
     */
    public AssignmentResponse getById(Long id) {
        log.debug("Получение задания: {}", id);
        return assignmentMapper.toResponse(
                findByIdOrThrow(id)
        );
    }

    /**
     * Получить задание с курсом (для детального просмотра)
     */
    public AssignmentWithCourseDto getByIdWithCourse(Long id) {
        log.debug("Получение задания с курсом: {}", id);
        return assignmentRepository.findByIdWithCourse(id)
                .map(assignmentMapper::toDtoWithCourse)
                .orElseThrow(() -> new NotFoundException("Задание не найдено: " + id));
    }

    /**
     * Получить ID курса для задания
     */
    public Long getCourseId(Long assignmentId) {
        return findByIdOrThrow(assignmentId).getCourse().getId();
    }

    // ========== СОЗДАНИЕ ==========

    /**
     * Создать новое задание
     */
    @Transactional
    public AssignmentResponse create(AssignmentCreateRequest request) {
        log.info("Создание нового задания для курса: {}", request.getCourseId());

        // Проверка курса
        Course course = getCourseById(request.getCourseId());

        // Проверка уникальности номера
        validateUniqueNumber(course, request.getNumber());

        // Проверка дедлайна
        validateDeadline(request.getDeadline());

        // Создание через маппер
        Assignment assignment = assignmentMapper.toEntity(request);
        assignment.setCourse(course); // Устанавливаем курс отдельно
        assignment.setIsActive(true); // По умолчанию активно

        assignment = assignmentRepository.save(assignment);

        log.info("Задание создано: {} (ID: {})", assignment.getTitle(), assignment.getId());
        return assignmentMapper.toResponse(assignment);
    }

    // ========== ОБНОВЛЕНИЕ ==========

    /**
     * Обновить задание
     */
    @Transactional
    public AssignmentResponse update(Long id, AssignmentUpdateRequest request) {
        log.info("Обновление задания: {}", id);

        Assignment assignment = findByIdOrThrow(id);

        // Проверка дедлайна
        if (request.getDeadline() != null) {
            validateDeadline(request.getDeadline());
        }

        // Обновление через маппер
        assignmentMapper.updateEntity(assignment, request);
        assignment = assignmentRepository.save(assignment);

        log.info("Задание обновлено: {}", id);
        return assignmentMapper.toResponse(assignment);
    }

    /**
     * Переключить активность задания
     */
    @Transactional
    public boolean toggleActive(Long id) {
        log.info("Переключение активности задания: {}", id);

        Assignment assignment = findByIdOrThrow(id);
        boolean newStatus = !assignment.getIsActive();
        assignment.setIsActive(newStatus);
        assignmentRepository.save(assignment);

        log.info("Статус задания {} изменен на: {}", id, newStatus);
        return newStatus;
    }

    // ========== УДАЛЕНИЕ ==========

    /**
     * Удалить задание
     */
    @Transactional
    public void delete(Long id) {
        log.info("Удаление задания: {}", id);

        Assignment assignment = findByIdOrThrow(id);

        // Проверка наличия сдач
        long submissionsCount = submissionRepository.countByAssignmentId(id);
        if (submissionsCount > 0) {
            throw new ValidationException(
                    String.format("Нельзя удалить задание '%s', по которому уже есть сдачи (%d)",
                            assignment.getTitle(), submissionsCount)
            );
        }

        assignmentRepository.delete(assignment);
        log.info("Задание удалено: {}", id);
    }

    // ========== ПОИСК ==========

    /**
     * Поиск заданий
     */
    public List<AssignmentResponse> search(String query) {
        log.debug("Поиск заданий: {}", query);
        return assignmentRepository.search(query).stream()
                .map(assignmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ========== ПАГИНАЦИЯ ==========

    /**
     * Получить задания курса с пагинацией
     */
    public Page<AssignmentResponse> getByCoursePaginated(Long courseId, Pageable pageable) {
        return assignmentRepository.findByCourseId(courseId, pageable)
                .map(assignmentMapper::toResponse);
    }

    // ========== СТАТИСТИКА ==========

    /**
     * Получить статистику по заданию
     */
    public AssignmentStatsDto getStats(Long assignmentId) {
        Assignment assignment = findByIdOrThrow(assignmentId);

        Long totalSubmissions = submissionRepository.countByAssignmentId(assignmentId);
        Long acceptedSubmissions = submissionRepository.countAcceptedByAssignmentId(assignmentId);
        Long pendingSubmissions = submissionRepository.countPendingByAssignmentId(assignmentId);
        Double averageScore = submissionRepository.getAverageScoreByAssignmentId(assignmentId);

        return AssignmentStatsDto.builder()
                .assignmentId(assignmentId)
                .title(assignment.getTitle())
                .number(assignment.getNumber())
                .totalSubmissions(totalSubmissions)
                .acceptedSubmissions(acceptedSubmissions)
                .pendingSubmissions(pendingSubmissions)
                .averageScore(averageScore != null ? averageScore : 0.0)
                .maxScore(assignment.getMaxScore())
                .completionRate(calculateCompletionRate(totalSubmissions, assignment))
                .build();
    }

    /**
     * Получить статистику по всем заданиям курса
     */
    public List<AssignmentStatsDto> getCourseStats(Long courseId) {
        Course course = getCourseById(courseId);
        List<Assignment> assignments = assignmentRepository.findAllByCourseId(courseId);

        return assignments.stream()
                .map(a -> getStats(a.getId()))
                .collect(Collectors.toList());
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private Assignment findByIdOrThrow(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Задание не найдено: " + id));
    }

    private Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Курс не найден: " + courseId));
    }

    private void validateUniqueNumber(Course course, Integer number) {
        Optional<Assignment> existing = assignmentRepository.findByCourseAndNumber(course, number);
        if (existing.isPresent()) {
            throw new ValidationException(
                    String.format("Задание с номером %d уже существует в курсе '%s'",
                            number, course.getName())
            );
        }
    }

    private void validateDeadline(LocalDateTime deadline) {
        if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дедлайн не может быть в прошлом");
        }
    }

    private Double calculateCompletionRate(Long totalSubmissions, Assignment assignment) {
        Long studentsInCourse = courseRepository.countActiveStudents(assignment.getCourse().getId());
        if (studentsInCourse == 0) {
            return 0.0;
        }
        return (totalSubmissions.doubleValue() / studentsInCourse) * 100;
    }
}
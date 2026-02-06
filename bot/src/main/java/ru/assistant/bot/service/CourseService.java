package ru.assistant.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.assistant.bot.model.Course;
import ru.assistant.bot.repository.AssignmentRepository;
import ru.assistant.bot.repository.CourseRepository;

import java.util.List;
import java.util.Optional;

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
}
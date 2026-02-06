package ru.assistant.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.assistant.bot.mapper.AssignmentMapper;
import ru.assistant.bot.model.Assignment;
import ru.assistant.bot.repository.AssignmentRepository;
import ru.assistant.bot.model.dto.AssignmentWithCourseDto;

import java.util.List;
import java.util.Optional;

/**
 * AssignmentService
 * @author agent
 * @since 03.02.2026
 */
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
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
}
package ru.assistant.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.assistant.bot.model.dto.StudentRatingDto;
import ru.assistant.bot.repository.StudentProgressRepository;
import ru.assistant.bot.repository.StudentRepository;
import ru.assistant.bot.repository.SubmissionRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RatingService — описание класса.
 * <p>
 * TODO: добавить описание назначения и поведения класса.
 * </p>
 *
 * @author agent
 * @since 10.02.2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {

    private final StudentRepository studentRepository;

    /**
     * Топ N студентов по среднему баллу
     */
    public List<StudentRatingDto> getTopStudentsByAverageScore(int limit) {
        return studentRepository.findTopStudentsByAverageScore(limit).stream()
                .map(this::mapToRatingDto)
                .collect(Collectors.toList());
    }

    /**
     * Топ N студентов по количеству принятых заданий
     */
    public List<StudentRatingDto> getTopStudentsByAcceptedSubmissions(int limit) {
        return studentRepository.findTopStudentsByAcceptedSubmissions(limit).stream()
                .map(this::mapToRatingDto)
                .collect(Collectors.toList());
    }

    /**
     * Рейтинг по конкретному курсу
     */
    public List<StudentRatingDto> getCourseRating(Long courseId, int limit) {
        List<Object[]> results = studentRepository.findTopStudentsByCourse(courseId, limit);

        List<StudentRatingDto> rating = new ArrayList<>();
        int rank = 1;

        for (Object[] row : results) {
            StudentRatingDto dto = StudentRatingDto.builder()
                    .studentId(((Number) row[0]).longValue())
                    .fullName((String) row[1])
                    .telegramUsername((String) row[2])
                    .githubUsername((String) row[3])
                    .assignmentsSubmitted(((Number) row[4]).intValue())
                    .assignmentsAccepted(((Number) row[5]).intValue())
                    .averageScore(row[6] != null ? new BigDecimal(row[6].toString()) : BigDecimal.ZERO)
                    .rank(rank++)
                    .build();
            rating.add(dto);
        }

        return rating;
    }

    /**
     * Позиция студента в общем рейтинге
     */
    public int getStudentRank(Long studentId) {
        List<StudentRatingDto> top100 = getTopStudentsByAverageScore(100);

        for (int i = 0; i < top100.size(); i++) {
            if (top100.get(i).getStudentId().equals(studentId)) {
                return i + 1;
            }
        }

        // Если не в топ-100, считаем позицию
        return studentRepository.calculateStudentRank(studentId).orElse(0);
    }

    private StudentRatingDto mapToRatingDto(Object[] row) {
        return StudentRatingDto.builder()
                .studentId(((Number) row[0]).longValue())
                .fullName((String) row[1])
                .telegramUsername((String) row[2])
                .githubUsername((String) row[3])
                .assignmentsSubmitted(((Number) row[4]).intValue())
                .assignmentsAccepted(((Number) row[5]).intValue())
                .averageScore(row[6] != null ? new BigDecimal(row[6].toString()) : BigDecimal.ZERO)
                .totalScore(((Number) row[7]).intValue())
                .build();
    }
}
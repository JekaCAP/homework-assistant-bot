package ru.assistant.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.assistant.bot.github.GitHubService;
import ru.assistant.bot.model.Assignment;
import ru.assistant.bot.model.Student;
import ru.assistant.bot.model.Submission;
import ru.assistant.bot.model.enums.SubmissionStatus;
import ru.assistant.bot.repository.AssignmentRepository;
import ru.assistant.bot.repository.SubmissionRepository;
import ru.assistant.bot.telegram.event.SubmissionCreatedEvent;
import ru.assistant.bot.telegram.event.SubmissionReviewedEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SubmissionService
 *
 * @author agent
 * @since 03.02.2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final StudentService studentService;
    private final GitHubService gitHubService;
    private final ApplicationEventPublisher eventPublisher;
    private final AssignmentRepository assignmentRepository;

    public Submission createSubmission(Long studentTelegramId, Long assignmentId, String prUrl) {
        Student student = studentService.findByTelegramId(studentTelegramId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        Assignment assignment = assignmentRepository.findByIdWithCourse(assignmentId)
                .orElseThrow(() -> new RuntimeException("Задание не найдено"));

        Optional<Submission> existingSubmission = submissionRepository
                .findTopByStudentIdAndAssignmentIdOrderBySubmittedAtDesc(student.getId(), assignmentId);

        Submission submission;

        if (existingSubmission.isPresent()) {
            Submission lastSubmission = existingSubmission.get();

            if (lastSubmission.getStatus() != SubmissionStatus.REJECTED &&
                lastSubmission.getStatus() != SubmissionStatus.NEEDS_REVISION &&
                lastSubmission.getSubmittedAt().isAfter(LocalDateTime.now().minusDays(7))) {

                throw new RuntimeException(
                        String.format("Вы уже сдавали это задание!\n\n" +
                                      "Задание: %s\n" +
                                      "Статус: %s\n" +
                                      "Оценка: %s\n\n" +
                                      "Если хотите пересдать, обратитесь к преподавателю.",
                                assignment.getTitle(),
                                lastSubmission.getStatus().getDisplayName(),
                                lastSubmission.getScore() != null ?
                                        lastSubmission.getScore() + "/100" : "не оценено"
                        )
                );
            }

            submission = lastSubmission;
            submission.setPrUrl(prUrl);
            submission.setStatus(SubmissionStatus.SUBMITTED);
            submission.setResubmittedAt(LocalDateTime.now());
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setScore(null);
            submission.setReviewerComment(null);

        } else {
            submission = new Submission();
            submission.setStudent(student);
            submission.setAssignment(assignment);
            submission.setPrUrl(prUrl);
            submission.setStatus(SubmissionStatus.SUBMITTED);
            submission.setSubmittedAt(LocalDateTime.now());
        }

        validatePrUrl(prUrl, student.getGithubUsername());

        Submission savedSubmission = submissionRepository.save(submission);

        log.info("✅ Сдача сохранена в БД с ID={}", savedSubmission.getId());

        eventPublisher.publishEvent(new SubmissionCreatedEvent(savedSubmission.getId()));

        return savedSubmission;
    }

    public Optional<Submission> findById(Long id) {
        return submissionRepository.findById(id);
    }

    public List<Submission> getPendingSubmissions() {
        return submissionRepository.findByStatusOrderBySubmittedAtDesc(SubmissionStatus.SUBMITTED);
    }

    public Submission reviewSubmission(Long submissionId, Integer score, String comment) {
        if (score == null) {
            log.error("❌ Передан null score для сдачи ID={}", submissionId);
            throw new IllegalArgumentException("Оценка не может быть null");
        }

        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Оценка должна быть от 0 до 100");
        }

        return submissionRepository.findById(submissionId)
                .map(submission -> {
                    log.info("🔄 Начинаем проверку сдачи ID={}, текущий статус={}, оценка={}",
                            submissionId, submission.getStatus(), score);

                    submission.setScore(score);
                    submission.setReviewerComment(comment != null ? comment : "Без комментария");
                    submission.setReviewedAt(LocalDateTime.now());

                    SubmissionStatus newStatus;
                    if (score >= 80) {
                        newStatus = SubmissionStatus.ACCEPTED;
                    } else if (score >= 60) {
                        newStatus = SubmissionStatus.NEEDS_REVISION;
                    } else {
                        newStatus = SubmissionStatus.REJECTED;
                    }

                    submission.setStatus(newStatus);

                    log.info("📝 Установлены параметры: score={}, status={}, comment={}",
                            score, newStatus, comment);

                    Submission savedSubmission = submissionRepository.save(submission);

                    log.info("✅ Сдача ID={} проверена и сохранена: score={}, status={}",
                            savedSubmission.getId(), savedSubmission.getScore(), savedSubmission.getStatus());

                    eventPublisher.publishEvent(new SubmissionReviewedEvent(savedSubmission.getId()));

                    return savedSubmission;
                })
                .orElseThrow(() -> {
                    log.error("❌ Сдача ID={} не найдена", submissionId);
                    return new RuntimeException("Submission not found with id: " + submissionId);
                });
    }

    public Map<String, Object> getSubmissionStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            long totalSubmissions = submissionRepository.count();
            long pendingSubmissions = submissionRepository.countByStatus(SubmissionStatus.SUBMITTED);
            long acceptedSubmissions = submissionRepository.countByStatus(SubmissionStatus.ACCEPTED);
            long rejectedSubmissions = submissionRepository.countRejectedSubmissions();
            long needsRevisionSubmissions = submissionRepository.countNeedsRevisionSubmissions();

            stats.put("totalSubmissions", totalSubmissions);
            stats.put("pendingSubmissions", pendingSubmissions);
            stats.put("acceptedSubmissions", acceptedSubmissions);
            stats.put("rejectedSubmissions", rejectedSubmissions);
            stats.put("needsRevisionSubmissions", needsRevisionSubmissions);
            stats.put("acceptanceRate", totalSubmissions > 0 ?
                    (double) acceptedSubmissions / totalSubmissions * 100 : 0);

        } catch (Exception e) {
            log.error("Error getting submission stats", e);
            stats.put("totalSubmissions", 0L);
            stats.put("pendingSubmissions", 0L);
            stats.put("acceptedSubmissions", 0L);
            stats.put("rejectedSubmissions", 0L);
            stats.put("needsRevisionSubmissions", 0L);
            stats.put("acceptanceRate", 0.0);
        }

        return stats;
    }

    public Long getStudentSubmissionsCount(Long studentId) {
        try {
            return submissionRepository.countByStudentId(studentId);
        } catch (Exception e) {
            log.error("Error counting student submissions", e);
            return 0L;
        }
    }

    private void validatePrUrl(String prUrl, String expectedGithubUsername) {
        if (!gitHubService.isValidPullRequestUrl(prUrl)) {
            throw new RuntimeException("Неверный формат ссылки на Pull Request");
        }

        GitHubService.PullRequestInfo prInfo;
        try {
            prInfo = gitHubService.getPullRequestInfo(prUrl);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось получить информацию о Pull Request. Проверьте ссылку.");
        }

        if (!prInfo.getIsOpen()) {
            throw new RuntimeException("Pull Request должен быть открыт");
        }

        if (expectedGithubUsername != null &&
            !prInfo.getAuthor().equals(expectedGithubUsername)) {
            log.warn("GitHub username mismatch: expected {}, got {}",
                    expectedGithubUsername, prInfo.getAuthor());
            throw new RuntimeException(
                    String.format("Автор Pull Request (@%s) не совпадает с вашим GitHub аккаунтом (@%s)",
                            prInfo.getAuthor(), expectedGithubUsername)
            );
        }
    }

    @Transactional(readOnly = true)
    public Optional<Submission> findByIdWithAllDetails(Long submissionId) {
        return submissionRepository.findByIdWithDetails(submissionId);
    }
}
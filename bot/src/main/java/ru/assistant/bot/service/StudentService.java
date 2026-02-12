package ru.assistant.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.assistant.bot.model.Course;
import ru.assistant.bot.model.Student;
import ru.assistant.bot.model.Submission;
import ru.assistant.bot.model.enums.SubmissionStatus;
import ru.assistant.bot.repository.CourseRepository;
import ru.assistant.bot.repository.StudentRepository;
import ru.assistant.bot.repository.SubmissionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * StudentService
 * @author agent
 * @since 03.02.2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final SubmissionRepository submissionRepository;

    public Student registerOrUpdateStudent(User telegramUser) {
        Long telegramId = telegramUser.getId();
        String username = telegramUser.getUserName();
        String fullName = getFullName(telegramUser);

        return studentRepository.findByTelegramId(telegramId)
                .map(existingStudent -> updateStudent(existingStudent, username, fullName))
                .orElseGet(() -> createStudent(telegramId, username, fullName));
    }

    public Optional<Student> findByTelegramId(Long telegramId) {
        return studentRepository.findByTelegramId(telegramId);
    }

    public Long getActiveStudentsCount() {
        try {
            return studentRepository.countByIsActiveTrue();
        } catch (Exception e) {
            log.error("Error getting active students count", e);
            return 0L;
        }
    }

    public List<Student> getTopStudents(int limit) {
        try {
            return studentRepository.findTopByIsActiveTrueOrderByRegistrationDateDesc(limit);
        } catch (Exception e) {
            log.error("Error getting top students", e);
            return new ArrayList<>();
        }
    }

    public List<Student> getTopActiveStudents(int limit) {
        try {
            return studentRepository.findTopActiveByLastActivity(limit);
        } catch (Exception e) {
            log.error("Error getting top active students", e);
            return getTopStudents(limit); // fallback
        }
    }

    public List<Map<String, Object>> getStudentsWithStats(int limit) {
        List<Student> students = getTopActiveStudents(limit);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Student student : students) {
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("id", student.getId());
            studentData.put("telegramId", student.getTelegramId());
            studentData.put("fullName", student.getFullName());
            studentData.put("githubUsername", student.getGithubUsername());
            studentData.put("registrationDate", student.getRegistrationDate());
            studentData.put("lastActivity", student.getLastActivity());

            Long submissionsCount = submissionRepository.countByStudentId(student.getId());
            studentData.put("submissionsCount", submissionsCount);

            result.add(studentData);
        }

        return result;
    }

    public Student updateGithubUsername(Long telegramId, String githubUsername) {
        Student student = studentRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        studentRepository.findByGithubUsername(githubUsername)
                .ifPresent(existingStudent -> {
                    if (!existingStudent.getId().equals(student.getId())) {
                        throw new RuntimeException("Этот GitHub аккаунт уже привязан к другому студенту");
                    }
                });

        student.setGithubUsername(githubUsername);
        student.setLastActivity(LocalDateTime.now());

        return studentRepository.save(student);
    }

    public Map<String, Object> getStudentProgressStats(Long telegramId) {
        Student student = studentRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        Map<String, Object> stats = new HashMap<>();

        List<Submission> submissions = submissionRepository.findByStudentTelegramId(telegramId);

        long totalSubmissions = submissions.size();
        long acceptedSubmissions = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                .count();

        double averageScore = submissions.stream()
                .filter(s -> s.getScore() != null)
                .mapToInt(Submission::getScore)
                .average()
                .orElse(0.0);

        Map<Course, List<Submission>> submissionsByCourse = new HashMap<>();
        for (Submission submission : submissions) {
            Course course = submission.getAssignment().getCourse();
            submissionsByCourse.computeIfAbsent(course, k -> new ArrayList<>()).add(submission);
        }

        List<Map<String, Object>> coursesProgress = new ArrayList<>();

        for (Map.Entry<Course, List<Submission>> entry : submissionsByCourse.entrySet()) {
            Course course = entry.getKey();
            List<Submission> courseSubmissions = entry.getValue();

            long courseAccepted = courseSubmissions.stream()
                    .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                    .count();

            double courseAverage = courseSubmissions.stream()
                    .filter(s -> s.getScore() != null)
                    .mapToInt(Submission::getScore)
                    .average()
                    .orElse(0.0);

            long totalAssignmentsInCourse = courseRepository.countAssignmentsByCourseId(course.getId());

            Map<String, Object> courseStats = new HashMap<>();
            courseStats.put("courseId", course.getId());
            courseStats.put("courseName", course.getName());
            courseStats.put("submitted", (long) courseSubmissions.size());
            courseStats.put("accepted", courseAccepted);
            courseStats.put("averageScore", courseAverage);
            courseStats.put("completionPercentage",
                    totalAssignmentsInCourse > 0 ?
                            (double) courseAccepted / totalAssignmentsInCourse * 100 : 0);

            coursesProgress.add(courseStats);
        }

        long rank = calculateStudentRank(student, acceptedSubmissions);

        stats.put("student", student);
        stats.put("totalSubmitted", totalSubmissions);
        stats.put("totalAccepted", acceptedSubmissions);
        stats.put("overallAverage", averageScore);
        stats.put("coursesProgress", coursesProgress);
        stats.put("rank", rank);
        stats.put("lastActivity", student.getLastActivity());

        return stats;
    }

    private long calculateStudentRank(Student student, long acceptedSubmissions) {
        List<Student> allActiveStudents = studentRepository.findByIsActiveTrue();

        List<Long> studentIds = allActiveStudents.stream()
                .map(Student::getId)
                .collect(Collectors.toList());

        List<Object[]> results = submissionRepository.countAcceptedSubmissionsByStudentIds(studentIds);

        Map<Long, Long> acceptedCountsByStudent = results.stream()
                .collect(Collectors.toMap(
                        obj -> (Long) obj[0],
                        obj -> (Long) obj[1]
                ));

        long studentsWithMoreAccepted = 0;

        for (Student otherStudent : allActiveStudents) {
            if (otherStudent.getId().equals(student.getId())) {
                continue;
            }

            long otherAccepted = acceptedCountsByStudent.getOrDefault(otherStudent.getId(), 0L);

            if (otherAccepted > acceptedSubmissions) {
                studentsWithMoreAccepted++;
            }
        }

        return studentsWithMoreAccepted + 1;
    }

    public List<Student> getActiveStudents() {
        return studentRepository.findByIsActiveTrue();
    }

    public List<Student> getStudentsWithGitHub() {
        return studentRepository.findStudentsWithGitHub();
    }

    public long countActiveStudents() {
        return studentRepository.countActiveStudents();
    }

    private Student createStudent(Long telegramId, String username, String fullName) {
        Student student = Student.builder()
                .telegramId(telegramId)
                .telegramUsername(username)
                .fullName(fullName)
                .registrationDate(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .isActive(true)
                .build();

        Student saved = studentRepository.save(student);
        log.info("Создан новый студент: {} (telegramId: {})", fullName, telegramId);
        return saved;
    }

    private Student updateStudent(Student student, String username, String fullName) {
        student.setTelegramUsername(username);
        student.setFullName(fullName);
        student.setLastActivity(LocalDateTime.now());

        Student updated = studentRepository.save(student);
        log.debug("Обновлен студент: {} (telegramId: {})", fullName, student.getTelegramId());
        return updated;
    }

    private String getFullName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? " " + user.getLastName() : "";
        return (firstName + lastName).trim();
    }

    public double calculateAverageScore(Long studentId) {
        try {
            Double avgScore = studentRepository.calculateAverageScore(studentId);
            return avgScore != null ? avgScore : 0.0;
        } catch (Exception e) {
            log.error("Error calculating average score for student {}", studentId, e);
            return 0.0;
        }
    }

    public int countAcceptedSubmissions(Long studentId) {
        try {
            return studentRepository.countAcceptedSubmissions(studentId);
        } catch (Exception e) {
            log.error("Error counting accepted submissions for student {}", studentId, e);
            return 0;
        }
    }
}
package ru.assistant.bot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.assistant.bot.model.ActivityLog;
import ru.assistant.bot.model.UserType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ActivityLogRepository
 * @author agent
 * @since 03.02.2026
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByUserIdAndUserTypeOrderByCreatedAtDesc(Long userId, UserType userType);

    List<ActivityLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT al FROM ActivityLog al WHERE al.createdAt >= :startDate " +
           "ORDER BY al.createdAt DESC")
    Page<ActivityLog> findRecentLogs(@Param("startDate") LocalDateTime startDate,
                                     Pageable pageable);

    @Query("SELECT al FROM ActivityLog al WHERE al.userId = :userId AND al.userType = :userType " +
           "AND al.action = :action " +
           "ORDER BY al.createdAt DESC")
    List<ActivityLog> findUserActions(@Param("userId") Long userId,
                                      @Param("userType") UserType userType,
                                      @Param("action") String action);

    @Query("SELECT COUNT(al) FROM ActivityLog al WHERE al.userType = :userType " +
           "AND al.createdAt BETWEEN :start AND :end")
    long countByUserTypeAndPeriod(@Param("userType") UserType userType,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    @Query("SELECT al.action, COUNT(al) as count FROM ActivityLog al " +
           "WHERE al.createdAt BETWEEN :start AND :end " +
           "GROUP BY al.action " +
           "ORDER BY count DESC")
    List<Object[]> findTopActions(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    @Query("SELECT al FROM ActivityLog al WHERE al.success = false " +
           "ORDER BY al.createdAt DESC")
    Page<ActivityLog> findErrors(Pageable pageable);

    @Query("SELECT DISTINCT al.action FROM ActivityLog al")
    List<String> findAllActions();

    @Query("SELECT al FROM ActivityLog al WHERE " +
           "(:userType IS NULL OR al.userType = :userType) AND " +
           "(:userId IS NULL OR al.userId = :userId) AND " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:success IS NULL OR al.success = :success) AND " +
           "al.createdAt BETWEEN :start AND :end " +
           "ORDER BY al.createdAt DESC")
    Page<ActivityLog> searchLogs(@Param("userType") UserType userType,
                                 @Param("userId") Long userId,
                                 @Param("action") String action,
                                 @Param("success") Boolean success,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 Pageable pageable);
}
package com.englishmemory.repository;

import com.englishmemory.entity.StudySession;
import com.englishmemory.enums.SessionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    Page<StudySession> findAllByUserIdAndActiveTrueOrderByStartedAtDesc(Long userId, Pageable pageable);

    List<StudySession> findAllByUserIdAndSessionTypeAndActiveTrue(Long userId, SessionType sessionType);

    @Query("""
            SELECT COALESCE(SUM(s.durationMinutes), 0) FROM StudySession s
            WHERE s.user.id = :userId
              AND s.active = true
            """)
    Long sumDurationMinutesByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COALESCE(SUM(s.durationMinutes), 0) FROM StudySession s
            WHERE s.user.id = :userId
              AND s.active = true
              AND s.startedAt >= :since
            """)
    Long sumDurationMinutesByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("""
            SELECT COUNT(DISTINCT CAST(s.startedAt AS LocalDate)) FROM StudySession s
            WHERE s.user.id = :userId
              AND s.active = true
              AND s.startedAt >= :since
            """)
    long countDistinctStudyDaysSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    List<StudySession> findAllByUserIdAndStartedAtBetweenAndActiveTrue(Long userId,
                                                                       LocalDateTime from,
                                                                       LocalDateTime to);
}

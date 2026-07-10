package com.englishmemory.repository;

import com.englishmemory.entity.ExerciseAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExerciseAttemptRepository extends JpaRepository<ExerciseAttempt, Long> {

    List<ExerciseAttempt> findAllByUserIdAndActiveTrue(Long userId);

    List<ExerciseAttempt> findAllByStudySessionIdAndActiveTrue(Long studySessionId);

    @Query("""
            SELECT COUNT(a) FROM ExerciseAttempt a
            WHERE a.user.id = :userId
              AND a.active = true
              AND a.isCorrect = true
            """)
    long countCorrectByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(a) FROM ExerciseAttempt a
            WHERE a.user.id = :userId
              AND a.active = true
              AND a.createdAt >= :since
            """)
    long countByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}

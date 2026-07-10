package com.englishmemory.repository;

import com.englishmemory.entity.ReviewSchedule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewScheduleRepository extends JpaRepository<ReviewSchedule, Long> {

    Optional<ReviewSchedule> findByVocabularyWordId(Long vocabularyWordId);

    Optional<ReviewSchedule> findByVocabularyWordIdAndActiveTrue(Long vocabularyWordId);

    @Query("""
            SELECT rs FROM ReviewSchedule rs
            JOIN rs.vocabularyWord v
            WHERE v.user.id = :userId
              AND rs.active = true
              AND rs.nextReviewDate <= :today
            ORDER BY rs.nextReviewDate ASC
            """)
    List<ReviewSchedule> findDueForReview(@Param("userId") Long userId,
                                          @Param("today") LocalDate today,
                                          Pageable pageable);

    @Query("""
            SELECT COUNT(rs) FROM ReviewSchedule rs
            JOIN rs.vocabularyWord v
            WHERE v.user.id = :userId
              AND rs.active = true
              AND rs.nextReviewDate <= :today
            """)
    long countDueForReview(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("""
            SELECT rs FROM ReviewSchedule rs
            JOIN rs.vocabularyWord v
            WHERE v.user.id = :userId
              AND rs.active = true
              AND rs.incorrectCount > 0
            ORDER BY rs.incorrectCount DESC
            """)
    List<ReviewSchedule> findTopMistakeWords(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT CAST(v.partOfSpeech AS string), SUM(rs.incorrectCount)
            FROM ReviewSchedule rs
            JOIN rs.vocabularyWord v
            WHERE v.user.id = :userId
              AND rs.active = true
              AND v.partOfSpeech IS NOT NULL
              AND rs.incorrectCount > 0
            GROUP BY v.partOfSpeech
            ORDER BY SUM(rs.incorrectCount) DESC
            """)
    List<Object[]> findWeakestPartOfSpeech(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(rs.correctCount), 0)
            FROM ReviewSchedule rs
            JOIN rs.vocabularyWord v
            WHERE v.user.id = :userId AND rs.active = true
            """)
    long sumCorrectCountByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COALESCE(SUM(rs.incorrectCount), 0)
            FROM ReviewSchedule rs
            JOIN rs.vocabularyWord v
            WHERE v.user.id = :userId AND rs.active = true
            """)
    long sumIncorrectCountByUserId(@Param("userId") Long userId);
}

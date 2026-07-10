package com.englishmemory.repository;

import com.englishmemory.entity.Progress;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Optional<Progress> findByUserIdAndVocabularyWordId(Long userId, Long vocabularyWordId);

    Optional<Progress> findByUserIdAndVocabularyWordIdAndActiveTrue(Long userId, Long vocabularyWordId);

    List<Progress> findAllByUserIdAndActiveTrueOrderByMasteryLevelAsc(Long userId, Pageable pageable);

    @Query("""
            SELECT COALESCE(AVG(p.masteryLevel), 0) FROM Progress p
            WHERE p.user.id = :userId
              AND p.active = true
            """)
    Double findAverageMasteryByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(p) FROM Progress p
            WHERE p.user.id = :userId
              AND p.active = true
              AND p.masteryLevel >= 80
            """)
    long countLearnedWordsByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(p) FROM Progress p
            WHERE p.user.id = :userId
              AND p.active = true
              AND p.masteryLevel < 40
            """)
    long countWeakWordsByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT v.cefrLevel, COUNT(p.id), COALESCE(AVG(p.masteryLevel), 0)
            FROM Progress p
            JOIN p.vocabularyWord v
            WHERE p.user.id = :userId
              AND p.active = true
              AND v.cefrLevel IS NOT NULL
            GROUP BY v.cefrLevel
            ORDER BY v.cefrLevel
            """)
    List<Object[]> findProgressGroupedByCefrLevel(@Param("userId") Long userId);

    @Query("""
            SELECT v.category.id, v.category.name, v.category.color,
                   COUNT(p.id), COALESCE(AVG(p.masteryLevel), 0)
            FROM Progress p
            JOIN p.vocabularyWord v
            WHERE p.user.id = :userId
              AND p.active = true
              AND v.category IS NOT NULL
            GROUP BY v.category.id, v.category.name, v.category.color
            ORDER BY v.category.name
            """)
    List<Object[]> findProgressGroupedByCategory(@Param("userId") Long userId);
}

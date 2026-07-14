package com.englishmemory.repository;

import com.englishmemory.entity.VocabularyWord;
import com.englishmemory.enums.CefrLevel;
import com.englishmemory.enums.PartOfSpeech;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VocabularyWordRepository extends JpaRepository<VocabularyWord, Long> {

    Page<VocabularyWord> findAllByUserIdAndActiveTrue(Long userId, Pageable pageable);

    Page<VocabularyWord> findAllByUserIdAndCategoryIdAndActiveTrue(Long userId, Long categoryId, Pageable pageable);

    Page<VocabularyWord> findAllByUserIdAndCefrLevelAndActiveTrue(Long userId, CefrLevel cefrLevel, Pageable pageable);

    Page<VocabularyWord> findAllByUserIdAndPartOfSpeechAndActiveTrue(Long userId, PartOfSpeech partOfSpeech, Pageable pageable);

    Optional<VocabularyWord> findByIdAndUserIdAndActiveTrue(Long id, Long userId);

    Optional<VocabularyWord> findByWordAndUserIdAndActiveFalse(String word, Long userId);

    boolean existsByWordAndUserIdAndActiveTrue(String word, Long userId);

    long countByUserIdAndActiveTrue(Long userId);

    long countByUserIdAndCategoryIdAndActiveTrue(Long userId, Long categoryId);

    @Query("""
            SELECT v FROM VocabularyWord v
            WHERE v.user.id = :userId
              AND v.active = true
              AND LOWER(v.word) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<VocabularyWord> searchByWordContaining(@Param("userId") Long userId,
                                                @Param("search") String search,
                                                Pageable pageable);

    @Query("""
            SELECT v FROM VocabularyWord v
            JOIN Progress p ON p.vocabularyWord.id = v.id
            WHERE v.user.id = :userId
              AND v.active = true
            ORDER BY p.masteryLevel ASC
            """)
    List<VocabularyWord> findWeakWordsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT v FROM VocabularyWord v
            JOIN Progress p ON p.vocabularyWord.id = v.id
            WHERE v.user.id = :userId
              AND v.active = true
              AND p.masteryLevel < :threshold
            ORDER BY p.masteryLevel ASC
            """)
    List<VocabularyWord> findGenuinelyWeakWordsByUserId(@Param("userId") Long userId,
                                                         @Param("threshold") int threshold,
                                                         Pageable pageable);

    @Query("""
            SELECT COUNT(v) FROM VocabularyWord v
            WHERE v.user.id = :userId
              AND v.active = true
              AND v.createdAt >= :since
            """)
    long countNewWordsSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}

package com.englishmemory.repository;

import com.englishmemory.entity.SentencePractice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SentencePracticeRepository extends JpaRepository<SentencePractice, Long> {

    Page<SentencePractice> findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("""
            SELECT AVG(sp.score) FROM SentencePractice sp
            WHERE sp.user.id = :userId
              AND sp.active = true
              AND sp.score IS NOT NULL
            """)
    Double findAverageScoreByUserId(@Param("userId") Long userId);

    long countByUserIdAndActiveTrue(Long userId);
}

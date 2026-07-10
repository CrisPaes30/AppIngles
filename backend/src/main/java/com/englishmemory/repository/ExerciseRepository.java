package com.englishmemory.repository;

import com.englishmemory.entity.Exercise;
import com.englishmemory.enums.ExerciseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    Page<Exercise> findAllByUserIdAndActiveTrue(Long userId, Pageable pageable);

    Page<Exercise> findAllByUserIdAndTypeAndActiveTrue(Long userId, ExerciseType type, Pageable pageable);

    Optional<Exercise> findByIdAndUserIdAndActiveTrue(Long id, Long userId);

    long countByUserIdAndActiveTrue(Long userId);
}

package com.englishmemory.repository;

import com.englishmemory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByUserIdAndActiveTrueOrderByNameAsc(Long userId);

    Optional<Category> findByIdAndUserIdAndActiveTrue(Long id, Long userId);

    boolean existsByNameAndUserIdAndActiveTrue(String name, Long userId);

    boolean existsByNameIgnoreCaseAndUserIdAndActiveTrue(String name, Long userId);

    long countByUserIdAndActiveTrue(Long userId);
}

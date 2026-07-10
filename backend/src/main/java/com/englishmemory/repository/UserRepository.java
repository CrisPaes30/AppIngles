package com.englishmemory.repository;

import com.englishmemory.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndActiveTrue(String email);

    boolean existsByEmailAndActiveTrue(String email);
}

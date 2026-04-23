package com.codingshuttle.projects.lovable_clone.repository;

import com.codingshuttle.projects.lovable_clone.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UsageLogRepository extends JpaRepository<UsageLog,Integer> {
    Optional<UsageLog> findByUserIdAndDate(Long userId, LocalDate today);
}

package com.example.hong.repository;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.entity.OwnerApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwnerApplicationRepository extends JpaRepository<OwnerApplication, Long> {
    List<OwnerApplication> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<OwnerApplication> findByStatusOrderByCreatedAtAsc(ApprovalStatus status);
    boolean existsByUserIdAndStatus(Long userId, ApprovalStatus status);
}
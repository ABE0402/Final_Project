package com.example.hong.service;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.entity.OwnerApplication;
import com.example.hong.entity.User;
import com.example.hong.repository.OwnerApplicationRepository;
import com.example.hong.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOwnerApplicationService {
    private final OwnerApplicationRepository appRepo;
    private final UserRepository userRepo;

    public List<OwnerApplication> listPending() {
        return appRepo.findByStatusOrderByCreatedAtAsc(ApprovalStatus.PENDING);
    }

    public OwnerApplication get(Long id) {
        return appRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("신청을 찾을 수 없습니다."));
    }

    @Transactional
    public void approve(Long appId, Long adminUserId, String note) {
        var app = get(appId);
        app.setStatus(ApprovalStatus.APPROVED);
        app.setReviewedBy(adminUserId);
        app.setReviewedAt(LocalDateTime.now());
        app.setRejectionReason(null);

        User u = userRepo.findById(app.getUserId()).orElseThrow();
        u.setRole(com.example.hong.domain.UserRole.OWNER);
    }

    @Transactional
    public void reject(Long appId, Long adminUserId, String reason) {
        if (reason == null || reason.isBlank())
            throw new IllegalArgumentException("반려 사유를 입력해 주세요.");
        var app = get(appId);
        app.setStatus(ApprovalStatus.REJECTED);
        app.setReviewedBy(adminUserId);
        app.setReviewedAt(LocalDateTime.now());
        app.setRejectionReason(reason);
    }
}

package com.example.hong.service;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.dto.OwnerApplyRequestDto;
import com.example.hong.entity.OwnerApplication;
import com.example.hong.repository.OwnerApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerApplicationService {

    private final OwnerApplicationRepository appRepo;

    @Transactional
    public Long submit(Long userId, OwnerApplyRequestDto req) {
        // 간단 검증
        if (req.getStoreType() == null) throw new IllegalArgumentException("가게 유형을 선택해 주세요.");
        if (isBlank(req.getStoreName())) throw new IllegalArgumentException("가게 이름을 입력해 주세요.");
        if (isBlank(req.getBusinessNumber())) throw new IllegalArgumentException("사업자번호를 입력해 주세요.");
        if (isBlank(req.getOwnerRealName())) throw new IllegalArgumentException("본명을 입력해 주세요.");
        if (isBlank(req.getContactPhone())) throw new IllegalArgumentException("연락처를 입력해 주세요.");
        if (isBlank(req.getStoreAddress())) throw new IllegalArgumentException("가게 주소를 입력해 주세요.");

        // 이미 대기중인 신청이 있으면 제한 (선택)
        if (appRepo.existsByUserIdAndStatus(userId, ApprovalStatus.PENDING)) {
            throw new IllegalStateException("승인 대기 중인 신청이 이미 있습니다.");
        }

        var now = LocalDateTime.now();
        var app = OwnerApplication.builder()
                .userId(userId)
                .storeType(req.getStoreType())
                .storeName(req.getStoreName())
                .businessNumber(req.getBusinessNumber())
                .ownerRealName(req.getOwnerRealName())
                .contactPhone(req.getContactPhone())
                .storeAddress(req.getStoreAddress())
                .status(ApprovalStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return appRepo.save(app).getId();
    }

    public List<OwnerApplication> myApplications(Long userId) {
        return appRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
}

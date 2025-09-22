package com.example.hong.service;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.entity.Cafe;
import com.example.hong.repository.CafeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AdminStoreService {

    private final CafeRepository cafeRepository;

    /* ========= Pending(승인 대기) ========= */
    @Transactional
    public List<Map<String,Object>> pendingList() {
        var list = cafeRepository.findByApprovalStatusOrderByCreatedAtAsc(ApprovalStatus.PENDING);
        return list.stream().map(this::toVm).toList();
    }

    @Transactional
    public Map<String,Object> pendingDetail(Long id) {
        Cafe c = get(id);
        if (c.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalArgumentException("대기 상태가 아닙니다.");
        }
        return toVm(c);
    }

    @Transactional
    public void approve(Long id, Long adminId, String note) {
        Cafe c = get(id);
        c.setApprovalStatus(ApprovalStatus.APPROVED);
        c.setRejectionReason(null);
        c.setVisible(true);
        // 필요하면 감사 로그 남기기(생략)
    }

    @Transactional
    public void reject(Long id, Long adminId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("반려 사유를 입력해 주세요.");
        }
        Cafe c = get(id);
        c.setApprovalStatus(ApprovalStatus.REJECTED);
        c.setRejectionReason(reason.trim());
        c.setVisible(false);
    }

    /* ========= Approved 관리 ========= */
    @Transactional
    public List<Map<String,Object>> approvedList(Boolean visible) {
        List<Cafe> list;
        if (visible == null) {
            list = cafeRepository.findByApprovalStatusOrderByUpdatedAtDesc(ApprovalStatus.APPROVED);
        } else {
            list = cafeRepository.findByApprovalStatusAndIsVisibleOrderByUpdatedAtDesc(ApprovalStatus.APPROVED, visible);
        }
        return list.stream().map(this::toVm).toList();
    }

    @Transactional
    public void show(Long id) {
        Cafe c = get(id);
        if (c.getApprovalStatus() != ApprovalStatus.APPROVED)
            throw new IllegalStateException("승인된 가게만 노출을 변경할 수 있습니다.");
        c.setVisible(true);
    }

    @Transactional
    public void hide(Long id) {
        Cafe c = get(id);
        if (c.getApprovalStatus() != ApprovalStatus.APPROVED)
            throw new IllegalStateException("승인된 가게만 노출을 변경할 수 있습니다.");
        c.setVisible(false);
    }

    /* ========= Helper ========= */
    private Cafe get(Long id) {
        return cafeRepository.findById(id).orElseThrow(() -> new NoSuchElementException("가게를 찾을 수 없습니다."));
    }

    private Map<String,Object> toVm(Cafe c) {
        Map<String,Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("name", nvl(c.getName()));
        m.put("address", nvl(c.getAddressRoad()));
        m.put("phone", nvl(c.getPhone()));
        m.put("ownerNick", c.getOwner()!=null ? nvlOr(c.getOwner().getNickname(), c.getOwner().getEmail()) : "-");
        m.put("status", c.getApprovalStatus().name());
        m.put("isPending",   c.getApprovalStatus()== ApprovalStatus.PENDING);
        m.put("isApproved",  c.getApprovalStatus()== ApprovalStatus.APPROVED);
        m.put("isRejected",  c.getApprovalStatus()== ApprovalStatus.REJECTED);
        m.put("visible", c.isVisible());
        m.put("createdAt", String.valueOf(c.getCreatedAt()));
        m.put("updatedAt", String.valueOf(c.getUpdatedAt()));
        m.put("heroImageUrl", c.getHeroImageUrl());
        m.put("rejectionReason", c.getRejectionReason());
        return m;
    }

    private String nvl(String s){ return s==null?"":s; }
    private String nvlOr(String a, String b){ return (a!=null && !a.isBlank()) ? a : (b==null?"":b); }
}


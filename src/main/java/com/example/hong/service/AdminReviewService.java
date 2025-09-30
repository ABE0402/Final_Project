// src/main/java/com/example/hong/service/AdminReviewService.java
package com.example.hong.service;

import com.example.hong.entity.Review;
import com.example.hong.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<Map<String, Object>> list(String q, String target, String status) {
        String query = (q == null) ? "" : q.trim();
        String tgt = (target == null || target.isBlank() || "ALL".equalsIgnoreCase(target)) ? null : target.toUpperCase();

        Boolean deleted = null;
        if ("ACTIVE".equalsIgnoreCase(status)) deleted = Boolean.FALSE;
        else if ("DELETED".equalsIgnoreCase(status)) deleted = Boolean.TRUE;

        var rows = reviewRepository.adminSearch(query, tgt, deleted);

        List<Map<String, Object>> vms = new ArrayList<>();
        for (Review r : rows) {
            boolean isCafe = r.getCafe() != null;
            String placeName = isCafe ? r.getCafe().getName() : (r.getRestaurant() != null ? r.getRestaurant().getName() : "-");
            Long placeId = isCafe ? r.getCafe().getId() : (r.getRestaurant() != null ? r.getRestaurant().getId() : null);

            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("targetType", isCafe ? "CAFE" : "RESTAURANT");
            m.put("placeName", nvl(placeName));
            if (placeId != null) m.put("placeId", placeId);

            String reviewer = r.getUser() != null
                    ? (r.getUser().getNickname() != null && !r.getUser().getNickname().isBlank()
                    ? r.getUser().getNickname()
                    : r.getUser().getEmail())
                    : "-";
            m.put("reviewer", nvl(reviewer));
            m.put("rating", r.getRating());
            m.put("content", nvl(r.getContent()));
            m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().format(DTF) : "-");

            String firstImg = firstNonNull(r.getImageUrl1(), r.getImageUrl2(), r.getImageUrl3(), r.getImageUrl4(), r.getImageUrl5());
            if (firstImg != null) m.put("imageUrl", firstImg);

            m.put("isDeleted", r.isDeleted());
            m.put("canDelete", !r.isDeleted());
            m.put("canRestore", r.isDeleted());

            vms.add(m);
        }
        return vms;
    }

    public void softDelete(Long id) {
        var r = reviewRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        if (!r.isDeleted()) {
            r.setDeleted(true);
        }
    }

    public void restore(Long id) {
        var r = reviewRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        if (r.isDeleted()) {
            r.setDeleted(false);
        }
    }

    private static String nvl(String s) { return (s == null || s.isBlank()) ? "-" : s; }

    @SafeVarargs
    private static <T> T firstNonNull(T... arr) {
        for (T t : arr) if (t != null) return t;
        return null;
    }
}

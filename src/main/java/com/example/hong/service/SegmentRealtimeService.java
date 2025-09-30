// src/main/java/com/example/hong/service/SegmentRealtimeService.java
package com.example.hong.service;

import com.example.hong.domain.AgeBucket;
import com.example.hong.domain.EventAction;
import com.example.hong.domain.Gender;
import com.example.hong.domain.SegmentType;
import com.example.hong.entity.CafeSegmentScore;
import com.example.hong.entity.User;
import com.example.hong.entity.UserEvent;
import com.example.hong.repository.CafeSegmentScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SegmentRealtimeService {

    private final CafeSegmentScoreRepository scoreRepo;

    // ✅ 가중치: 스케줄 재계산(반감기 적용)과 동일 기조 유지
    private static double weight(EventAction a) {
        return switch (a) {
            case CLICK    -> 1.0;
            case FAVORITE -> 5.0;
            case RESERVE  -> 10.0;
            case REVIEW   -> 8.0;
        };
    }

    /** 이벤트 저장 직후 호출 → 실시간으로 score_30d 가산(감쇠는 스케줄러에서 처리) */
    @Transactional
    public void apply(UserEvent e) {
        User u = e.getUser();
        if (u == null || u.getBirthDate() == null || u.getGender() == null) return;

        double delta = weight(e.getAction());
        if (e.getAction() == EventAction.REVIEW && e.getRatingValue() != null) {
            // 리뷰 별점 보너스(3.5 초과만)
            delta += Math.max(0, 2.0 * (e.getRatingValue() - 3.5));
        }

        Long cafeId = e.getCafe().getId();
        AgeBucket age = AgeBucket.fromBirthDate(u.getBirthDate());
        Gender g = u.getGender();

        if (age != null) {
            addDelta(SegmentType.AGE, age.code(), cafeId, delta);
        }
        if (g == Gender.MALE || g == Gender.FEMALE) {
            addDelta(SegmentType.GENDER, g.name(), cafeId, delta);
        }
    }

    private void addDelta(SegmentType type, String val, Long cafeId, double delta) {
        try {
            // MySQL ON DUPLICATE KEY
            scoreRepo.addScoreDelta(type.name(), val, cafeId, delta);
        } catch (DataAccessException | UnsupportedOperationException ex) {
            // ✅ JPA 폴백 (H2 등) — find→add→save
            var id = new CafeSegmentScore.Id(type, val, cafeId);
            var row = scoreRepo.findById(id).orElseGet(() ->
                    CafeSegmentScore.builder().id(id).score30d(0.0).build()
            );
            row.setScore30d(row.getScore30d() + delta);
            scoreRepo.save(row);
        }
    }
}

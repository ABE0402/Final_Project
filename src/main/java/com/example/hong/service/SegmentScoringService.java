// src/main/java/com/example/hong/service/SegmentScoringService.java
package com.example.hong.service;

import com.example.hong.domain.AgeBucket;
import com.example.hong.domain.EventAction;
import com.example.hong.domain.Gender;
import com.example.hong.domain.SegmentType;
import com.example.hong.entity.CafeSegmentScore;
import com.example.hong.entity.User;
import com.example.hong.entity.UserEvent;
import com.example.hong.repository.CafeSegmentScoreRepository;
import com.example.hong.repository.UserEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SegmentScoringService {

    private final UserEventRepository userEventRepository;
    private final CafeSegmentScoreRepository cafeSegmentScoreRepository;


    private static final Map<EventAction, Integer> HALF_LIFE = Map.of(
            EventAction.CLICK, 7,
            EventAction.FAVORITE, 30,
            EventAction.RESERVE, 45,
            EventAction.REVIEW, 60
    );
    private static final Map<EventAction, Double> WEIGHT = Map.of(
            EventAction.CLICK, 1.0,
            EventAction.FAVORITE, 5.0,
            EventAction.RESERVE, 10.0,
            EventAction.REVIEW, 8.0
    );

    @Transactional
    @Scheduled(cron = "0 */30 * * * *") // 매 30분
    public void recomputeScores() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusDays(60);

        var actions = List.of(EventAction.CLICK, EventAction.FAVORITE, EventAction.RESERVE, EventAction.REVIEW);
        List<UserEvent> events = userEventRepository.findRecentByActions(since, actions);

        // key: (segmentType, segmentValue, cafeId)
        Map<CafeSegmentScore.Id, Double> acc = new HashMap<>();

        for (UserEvent e : events) {
            User u = e.getUser();
            if (u == null || u.getBirthDate() == null || u.getGender() == null) continue;

            // 나이 버킷
            AgeBucket age = AgeBucket.fromBirthDate(u.getBirthDate());
            // 성별 (UNKNOWN 제외)
            Gender g = u.getGender();
            boolean genderOk = (g == Gender.MALE || g == Gender.FEMALE);

            double base = WEIGHT.getOrDefault(e.getAction(), 0.0);
            int hl = HALF_LIFE.getOrDefault(e.getAction(), 30);
            double days = Math.max(0.0, Duration.between(e.getCreatedAt(), now).toHours() / 24.0);
            double decay = Math.pow(0.5, days / hl);

            double plus = base * decay;

            // 리뷰 별점 보너스(이벤트 단위, 3.5 초과만 가점)
            if (e.getAction() == EventAction.REVIEW && e.getRatingValue() != null) {
                double bonus = Math.max(0, 2.0 * (e.getRatingValue() - 3.5));
                plus += bonus * decay;
            }

            Long cafeId = e.getCafe().getId();

            if (age != null) {
                var id = CafeSegmentScore.Id.builder()
                        .segmentType(SegmentType.AGE)
                        .segmentValue(age.code())
                        .cafeId(cafeId)
                        .build();
                acc.merge(id, plus, Double::sum);
            }
            if (genderOk) {
                var id = CafeSegmentScore.Id.builder()
                        .segmentType(SegmentType.GENDER)
                        .segmentValue(g.name())
                        .cafeId(cafeId)
                        .build();
                acc.merge(id, plus, Double::sum);
            }
        }

        List<CafeSegmentScore> upserts = new ArrayList<>(acc.size());
        acc.forEach((id, score) -> upserts.add(
                CafeSegmentScore.builder().id(id).score30d(score).build()
        ));
        cafeSegmentScoreRepository.saveAll(upserts);
    }
}

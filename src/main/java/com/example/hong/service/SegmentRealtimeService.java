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


    private static double weight(EventAction a) {
        return switch (a) {
            case CLICK    -> 1.0;
            case FAVORITE -> 5.0;
            case RESERVE  -> 10.0;
            case REVIEW   -> 8.0;
        };
    }

    @Transactional
    public void apply(UserEvent e) {
        User u = e.getUser();
        if (u == null || u.getBirthDate() == null || u.getGender() == null) return;

        double delta = weight(e.getAction());
        if (e.getAction() == EventAction.REVIEW && e.getRatingValue() != null) {

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

            scoreRepo.addScoreDelta(type.name(), val, cafeId, delta);
        } catch (DataAccessException | UnsupportedOperationException ex) {

            var id = new CafeSegmentScore.Id(type, val, cafeId);
            var row = scoreRepo.findById(id).orElseGet(() ->
                    CafeSegmentScore.builder().id(id).score30d(0.0).build()
            );
            row.setScore30d(row.getScore30d() + delta);
            scoreRepo.save(row);
        }
    }
}

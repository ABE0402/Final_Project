package com.example.hong.repository;

import com.example.hong.domain.SegmentType;
import com.example.hong.entity.CafeSegmentScore;
import org.springframework.data.annotation.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CafeSegmentScoreRepository extends JpaRepository<CafeSegmentScore, CafeSegmentScore.Id> {


    List<CafeSegmentScore> findTop50ById_SegmentTypeAndId_SegmentValueOrderByScore30dDesc(
            SegmentType segmentType, String segmentValue);

    @Modifying
    @Query(value = """
        INSERT INTO cafe_segment_scores(segment_type, segment_value, cafe_id, score_30d, updated_at)
        VALUES (:stype, :sval, :cafeId, :delta, NOW())
        ON DUPLICATE KEY UPDATE
            score_30d = score_30d + VALUES(score_30d),
            updated_at = NOW()
    """, nativeQuery = true)
    void addScoreDelta(@Param("stype") String segmentType,
                       @Param("sval") String segmentValue,
                       @Param("cafeId") Long cafeId,
                       @Param("delta") double delta);
}
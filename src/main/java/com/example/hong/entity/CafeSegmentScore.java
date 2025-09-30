// src/main/java/com/example/hong/entity/CafeSegmentScore.java
package com.example.hong.entity;

import com.example.hong.domain.SegmentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "cafe_segment_scores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CafeSegmentScore {

    @EmbeddedId
    private Id id;

    @Column(name = "score_30d", nullable = false)
    private double score30d;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Embeddable @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Id implements Serializable {
        @Enumerated(EnumType.STRING)
        @Column(name = "segment_type", length = 10, nullable = false)
        private SegmentType segmentType;

        @Column(name = "segment_value", length = 20, nullable = false)
        private String segmentValue; // "20S" / "FEMALE"

        @Column(name = "cafe_id", nullable = false)
        private Long cafeId;
    }
}

package com.example.hong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(
        name = "cafe_tags",
        uniqueConstraints = @UniqueConstraint(name = "uk_cafe_tag", columnNames = {"cafe_id", "tag_id"}),
        indexes = {
                @Index(name = "idx_cafe_tags_tag_cafe", columnList = "tag_id,cafe_id")
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CafeTag {

    @EmbeddedId
    private CafeTagId id;

    @MapsId("cafeId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id", nullable = false)
    private Cafe cafe;

    @MapsId("tagId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    /* 팩토리 */
    public static CafeTag of(Cafe cafe, Tag tag) {
        CafeTag ct = new CafeTag();
        ct.cafe = cafe;
        ct.tag = tag;
        ct.id = new CafeTagId(cafe.getId(), tag.getId());
        return ct;
    }

    /* ===== 복합키 ===== */
    @Embeddable
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class CafeTagId implements Serializable {
        @Column(name = "cafe_id")
        private Long cafeId;
        @Column(name = "tag_id")
        private Integer tagId;
    }

    public void setCafe(Cafe cafe){ this.cafe = cafe; }
    public void setTag(Tag tag){ this.tag = tag; }


}
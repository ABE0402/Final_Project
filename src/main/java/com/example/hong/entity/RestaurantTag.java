
// src/main/java/com/example/hong/entity/RestaurantTag.java
package com.example.hong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(
        name = "restaurant_tags",
        uniqueConstraints = @UniqueConstraint(name = "uk_restaurant_tag", columnNames = {"restaurant_id", "tag_id"}),
        indexes = {
                @Index(name = "idx_restaurant_tags_tag_rest", columnList = "tag_id,restaurant_id")
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class RestaurantTag {

    @EmbeddedId
    private RestaurantTagId id;

    @MapsId("restaurantId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @MapsId("tagId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public static RestaurantTag of(Restaurant r, Tag t) {
        RestaurantTag rt = new RestaurantTag();
        rt.restaurant = r;
        rt.tag = t;
        rt.id = new RestaurantTagId(r.getId(), t.getId());
        return rt;
    }

    @Embeddable
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class RestaurantTagId implements Serializable {
        @Column(name = "restaurant_id")
        private Long restaurantId;
        @Column(name = "tag_id")
        private Integer tagId;
    }
}


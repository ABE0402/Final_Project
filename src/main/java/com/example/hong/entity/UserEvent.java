package com.example.hong.entity;


import com.example.hong.domain.EventAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_events",
        indexes = {
                @Index(name="idx_ue_cafe_time", columnList="cafe_id,created_at"),
                @Index(name="idx_ue_user_time", columnList="user_id,created_at"),
                @Index(name="idx_ue_action_time", columnList="action,created_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cafe_id", nullable = false)
    private Cafe cafe;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EventAction action;

    /** REVIEW일 때만 채움(1~5). 그 외 null */
    @Column(name = "rating_value")
    private Integer ratingValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static UserEvent click(User u, Cafe c){
        return UserEvent.builder().user(u).cafe(c).action(EventAction.CLICK).build();
    }
    public static UserEvent favorite(User u, Cafe c){
        return UserEvent.builder().user(u).cafe(c).action(EventAction.FAVORITE).build();
    }
    public static UserEvent reserve(User u, Cafe c){
        return UserEvent.builder().user(u).cafe(c).action(EventAction.RESERVE).build();
    }
    public static UserEvent review(User u, Cafe c, int rating){
        return UserEvent.builder().user(u).cafe(c).action(EventAction.REVIEW).ratingValue(rating).build();
    }
}
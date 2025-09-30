package com.example.hong.repository;

import com.example.hong.domain.EventAction;
import com.example.hong.entity.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    List<UserEvent> findByCreatedAtAfter(LocalDateTime since);

    @Query("""
        select e from UserEvent e
        where e.createdAt >= :since
          and e.action in :actions
    """)
    List<UserEvent> findRecentByActions(@Param("since") LocalDateTime since,
                                        @Param("actions") Collection<EventAction> actions);
}
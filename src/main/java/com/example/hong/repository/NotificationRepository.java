package com.example.hong.repository;

import com.example.hong.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {


    long countByUser_IdAndReadFalse(Long userId);

    List<Notification> findTop30ByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<Notification> findByIdAndUser_Id(Long id, Long userId);

    @Modifying
    @Query("update Notification n set n.read = true where n.user.id = :uid and n.read = false")
    int markAllRead(@Param("uid") Long userId);

    @Modifying
    @Query("update Notification n set n.read = true where n.id = :id and n.user.id = :uid")
    int markOneRead(@Param("uid") Long userId, @Param("id") Long notificationId);

    @Modifying
    int deleteByUser_IdAndIdIn(Long userId, List<Long> ids);

    @Modifying
    int deleteByUser_Id(Long userId);
}


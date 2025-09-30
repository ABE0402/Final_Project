package com.example.hong.service;

import com.example.hong.entity.Notification;
import com.example.hong.entity.User;
import com.example.hong.repository.NotificationRepository;
import com.example.hong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification push(Long userId, String message, String linkUrl) {
        User u = userRepository.findById(userId).orElseThrow();
        return notificationRepository.save(
                Notification.builder()
                        .user(u).message(message).linkUrl(linkUrl)
                        .read(false).build()
        );
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByUser_IdAndReadFalse(userId);
    }

    public List<Notification> recent(Long userId) {
        return notificationRepository.findTop30ByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        notificationRepository.markOneRead(userId, notificationId);
    }

    // 보이는 알림(전달받은 ids)만 삭제
    @Transactional
    public int deleteByIds(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        return notificationRepository.deleteByUser_IdAndIdIn(userId, ids);
    }

    // 내 알림 전부 삭제
    @Transactional
    public int deleteAll(Long userId) {
        return notificationRepository.deleteByUser_Id(userId);
    }




}
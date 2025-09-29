package com.example.hong.controller;

import com.example.hong.entity.Notification;
import com.example.hong.service.NotificationService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    private Long meId(Authentication a) {
        return (a != null && a.isAuthenticated() && !(a.getPrincipal() instanceof String))
                ? ((AppUserPrincipal) a.getPrincipal()).getId()
                : null;
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication auth) {
        Long uid = meId(auth);
        if (uid == null) return Map.of("count", 0L);
        return Map.of("count", notificationService.unreadCount(uid));
    }

    @GetMapping("/recent")
    public List<Map<String, Object>> recent(Authentication auth) {
        Long uid = meId(auth);
        if (uid == null) return List.of();
        return notificationService.recent(uid).stream().map(this::toDto).toList();
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(Authentication auth) {
        Long uid = meId(auth);
        if (uid == null) return ResponseEntity.status(401).build();
        notificationService.markAllRead(uid);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id, Authentication auth) {
        Long uid = meId(auth);
        if (uid == null) return ResponseEntity.status(401).build();
        notificationService.markRead(uid, id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private Map<String, Object> toDto(Notification n) {
        return Map.of(
                "id", n.getId(),
                "message", n.getMessage(),
                "link", n.getLinkUrl(),
                "read", n.isRead(),
                "createdAt", n.getCreatedAt().toString()
        );
    }
}

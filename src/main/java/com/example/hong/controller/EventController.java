package com.example.hong.controller;

import com.example.hong.entity.Cafe;
import com.example.hong.entity.User;
import com.example.hong.entity.UserEvent;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.UserEventRepository;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final UserEventRepository userEventRepository;
    private final CafeRepository cafeRepository;

    @PostMapping("/e/click")
    public ResponseEntity<Void> logClick(@RequestParam Long cafeId, Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal p)) return ResponseEntity.noContent().build();
        User u = new User(); u.setId(p.getId()); // 프록시 최소화(영속 필요 없으면 ID만)
        Cafe c = cafeRepository.findById(cafeId).orElse(null);
        if (c == null) return ResponseEntity.noContent().build();
        userEventRepository.save(UserEvent.click(u, c));
        return ResponseEntity.noContent().build();
    }
}
// src/main/java/com/example/hong/controller/ReservationController.java
package com.example.hong.controller;

import com.example.hong.domain.ReserveTargetType;
import com.example.hong.entity.Cafe;                      // ★ 추가
import com.example.hong.entity.Reservation;
import com.example.hong.entity.UserEvent;               // ★ 추가
import com.example.hong.repository.CafeRepository;      // ★ 추가
import com.example.hong.repository.UserEventRepository; // ★ 추가
import com.example.hong.repository.UserRepository;      // ★ 추가
import com.example.hong.service.ReservationService;
import com.example.hong.service.SegmentRealtimeService; // ★ 추가
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // ★ 이벤트 적재용 의존성
    private final UserRepository userRepository;               // ★ 추가
    private final CafeRepository cafeRepository;               // ★ 추가
    private final UserEventRepository userEventRepository;     // ★ 추가
    private final SegmentRealtimeService segmentRealtimeService; // ★ 추가

    private Long meId(Authentication a) {
        return (a != null && a.isAuthenticated() && !(a.getPrincipal() instanceof String))
                ? ((AppUserPrincipal) a.getPrincipal()).getId()
                : null;
    }

    @GetMapping("/reserve")
    public String reservePage(@RequestParam("type") ReserveTargetType type,
                              @RequestParam("id") Long targetId,
                              Model model) {
        model.addAttribute("type", type.name());
        model.addAttribute("targetId", targetId);
        return "reserve";
    }

    @PostMapping(value = "/api/reservations", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody CreateReservationReq req, Authentication auth) {
        Long userId = meId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message","로그인이 필요합니다."));

        LocalDateTime at = LocalDateTime.parse(req.getReservationAt(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        Reservation saved = reservationService.create(userId, req.getTargetType(), req.getTargetId(), at, req.getPartySize());

        // ✅ 예약 이벤트 (카페일 때만)
        if (req.getTargetType() == ReserveTargetType.CAFE) {
            try {
                var user = userRepository.getReferenceById(userId);
                var cafe = cafeRepository.getReferenceById(req.getTargetId());
                var ev = userEventRepository.save(UserEvent.reserve(user, cafe));
                segmentRealtimeService.apply(ev);
            } catch (Exception ignore) {}
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.LOCATION, "/mypage/reservations")
                .body(Map.of(
                        "reservationId", saved.getId(),
                        "status", saved.getStatus().name(),
                        "reservationAt", saved.getReservationAt().toString()
                ));
    }

    @PostMapping(value = "/reservations", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String createAndRedirect(CreateReservationForm form,
                                    Authentication auth,
                                    RedirectAttributes ra) {
        Long userId = meId(auth);
        if (userId == null) return "redirect:/login";

        LocalDateTime at = LocalDateTime.parse(form.getReservationAt(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        reservationService.create(userId, form.getTargetType(), form.getTargetId(), at, form.getPartySize());

        // ✅ 예약 이벤트 (카페일 때만)
        if (form.getTargetType() == ReserveTargetType.CAFE) {
            try {
                var user = userRepository.getReferenceById(userId);
                var cafe = cafeRepository.getReferenceById(form.getTargetId());
                var ev = userEventRepository.save(UserEvent.reserve(user, cafe));
                segmentRealtimeService.apply(ev);
            } catch (Exception ignore) {}
        }

        ra.addFlashAttribute("toast", "예약이 완료되었습니다.");
        return "redirect:/mypage/reservations";
    }

    @PostMapping("/api/reservations/{reservationId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancel(@PathVariable Long reservationId, Authentication auth) {
        Long userId = meId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message","로그인이 필요합니다."));
        reservationService.cancel(userId, reservationId);
        return ResponseEntity.ok(Map.of("message","취소되었습니다."));
    }

    @Data
    public static class CreateReservationReq {
        private ReserveTargetType targetType; // CAFE/RESTAURANT
        private Long targetId;
        private String reservationAt; // "YYYY-MM-DD'T'HH:mm"
        private int partySize;
    }

    @Data
    public static class CreateReservationForm {
        private ReserveTargetType targetType; // CAFE/RESTAURANT
        private Long targetId;
        private String reservationAt; // "YYYY-MM-DD'T'HH:mm"
        private int partySize;
    }
}

package com.example.hong.controller;

import com.example.hong.domain.ReserveTargetType;
import com.example.hong.entity.Reservation;
import com.example.hong.service.ReservationService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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

    private Long meId(Authentication a) {
        return (a != null && a.isAuthenticated() && !(a.getPrincipal() instanceof String))
                ? ((AppUserPrincipal) a.getPrincipal()).getId()
                : null;
    }

    /** 공통 예약 페이지: /reserve?type=CAFE&id=123 */
    @GetMapping("/reserve")
    public String reservePage(@RequestParam("type") ReserveTargetType type,
                              @RequestParam("id") Long targetId,
                              Model model) {
        model.addAttribute("type", type.name());
        model.addAttribute("targetId", targetId);
        return "reserve";
    }

    /** 예약 생성(JSON API) - 기존 동작 유지 */
    @PostMapping(value = "/api/reservations", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody CreateReservationReq req, Authentication auth) {
        Long userId = meId(auth);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("message","로그인이 필요합니다."));

        LocalDateTime at = LocalDateTime.parse(req.getReservationAt(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        Reservation saved = reservationService.create(userId, req.getTargetType(), req.getTargetId(), at, req.getPartySize());

        // JSON 본문은 그대로, 참고용 Location 헤더를 함께 제공 (클라이언트가 필요 시 따라가도록)
        return ResponseEntity.ok()
                .header(HttpHeaders.LOCATION, "/mypage/reservations")
                .body(Map.of(
                        "reservationId", saved.getId(),
                        "status", saved.getStatus().name(),
                        "reservationAt", saved.getReservationAt().toString()
                ));
    }

    /** 예약 생성(폼 제출) → 즉시 마이페이지/예약내역으로 리다이렉트 */
    @PostMapping(value = "/reservations", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String createAndRedirect(CreateReservationForm form,
                                    Authentication auth,
                                    RedirectAttributes ra) {
        Long userId = meId(auth);
        if (userId == null) return "redirect:/login";

        LocalDateTime at = LocalDateTime.parse(form.getReservationAt(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        reservationService.create(userId, form.getTargetType(), form.getTargetId(), at, form.getPartySize());

        ra.addFlashAttribute("toast", "예약이 완료되었습니다.");
        return "redirect:/mypage/reservations";
    }

    /** 예약 취소(JSON) */
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

    /** 폼 전송용 DTO */
    @Data
    public static class CreateReservationForm {
        private ReserveTargetType targetType; // CAFE/RESTAURANT
        private Long targetId;
        private String reservationAt; // "YYYY-MM-DD'T'HH:mm"
        private int partySize;
    }
}

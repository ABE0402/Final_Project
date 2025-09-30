package com.example.hong.controller;

import com.example.hong.service.OwnerReviewService;
import com.example.hong.service.OwnerService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/owner")
@PreAuthorize("hasAnyRole('OWNER','ADMIN')")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;
    private final OwnerReviewService ownerReviewService;

    private Long meId(Authentication auth) {
        return ((AppUserPrincipal) auth.getPrincipal()).getId();
    }
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // 점주 메인
    @GetMapping({"", "/"})
    public String home() {
        return "redirect:/owner/reviews";
    }

    @GetMapping("/reviews")
    public String reviews(Authentication auth, Model model) {
        model.addAttribute("tabOwnerReviews", true);
        model.addAttribute("reviews", ownerReviewService.listForOwner(meId(auth)));
        return "owner/reviews";
    }

    @PostMapping("/reviews/{reviewId}/reply")
    public String upsertReply(@PathVariable Long reviewId, @RequestParam String content, Authentication auth) {
        ownerReviewService.upsertReply(meId(auth), reviewId, content);
        return "redirect:/owner/reviews";
    }

    @PostMapping("/reviews/{reviewId}/reply/delete")
    public String deleteReply(@PathVariable Long reviewId, Authentication auth) {
        ownerReviewService.deleteReply(meId(auth), reviewId);
        return "redirect:/owner/reviews";
    }

    @GetMapping("/reservations")
    public String reservations(Authentication auth, Model model) {
        Long ownerId = ((AppUserPrincipal) auth.getPrincipal()).getId();
        var list = ownerService.listReservations(ownerId); // ✅ 단일 DTO 리스트
        model.addAttribute("tabOwnerReservations", true);
        model.addAttribute("reservations", list);          // ✅ 템플릿 키 일치
        return "owner/reservations";
    }

    @PostMapping("/reservations/{id}/confirm")
    public String confirm(@PathVariable Long id, Authentication auth) {
        ownerService.confirm(meId(auth), id, isAdmin(auth));
        return "redirect:/owner/reservations";
    }

    @PostMapping("/reservations/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication auth) {
        ownerService.ownerCancel(meId(auth), id, isAdmin(auth));
        return "redirect:/owner/reservations";
    }
}

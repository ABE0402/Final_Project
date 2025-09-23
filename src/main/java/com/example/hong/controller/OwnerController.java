package com.example.hong.controller;

import com.example.hong.service.OwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/owner")
@PreAuthorize("hasAnyRole('OWNER','ADMIN')")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    // 점주 메인(리뷰 탭 기본)
    @GetMapping({"","/"})
    public String home(Model model) {
        model.addAttribute("tabOwnerReviews", true);
        model.addAttribute("reviews", java.util.Collections.emptyList()); // TODO: 서비스 연동
        return "owner/index";
    }

    @GetMapping("/reviews")
    public String reviews(Model model) {
        model.addAttribute("tabOwnerReviews", true);
        model.addAttribute("reviews", java.util.Collections.emptyList());
        return "owner/index";
    }


    @GetMapping("/reservations")
    public String reservations(Model model) {
        model.addAttribute("tabOwnerReservations", true);
        model.addAttribute("reservations", java.util.Collections.emptyList());
        return "owner/index";
    }
}
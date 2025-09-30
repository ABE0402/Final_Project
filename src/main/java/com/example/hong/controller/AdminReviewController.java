package com.example.hong.controller;

import com.example.hong.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    // 목록 + 필터
    @GetMapping({"", "/"})
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "target", required = false, defaultValue = "ALL") String target,
                       @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
                       @RequestParam(value = "msg", required = false) String msg,
                       @RequestParam(value = "err", required = false) String err,
                       Model model) {

        var vm = adminReviewService.list(q, target, status);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("target", target);
        model.addAttribute("status", status);


        model.addAttribute("equalsCafe", "CAFE".equalsIgnoreCase(target));
        model.addAttribute("equalsRestaurant", "RESTAURANT".equalsIgnoreCase(target));
        model.addAttribute("equalsAllTarget", !"CAFE".equalsIgnoreCase(target) && !"RESTAURANT".equalsIgnoreCase(target));

        model.addAttribute("equalsActive", "ACTIVE".equalsIgnoreCase(status));
        model.addAttribute("equalsDeleted", "DELETED".equalsIgnoreCase(status));
        model.addAttribute("equalsAllStatus", !"ACTIVE".equalsIgnoreCase(status) && !"DELETED".equalsIgnoreCase(status));

        model.addAttribute("reviews", vm);
        if (msg != null) model.addAttribute("msg", msg);
        if (err != null) model.addAttribute("err", err);

        return "admin/reviews";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminReviewService.softDelete(id);
            ra.addAttribute("msg", "리뷰를 삭제(숨김)했습니다.");
        } catch (Exception e) {
            ra.addAttribute("err", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminReviewService.restore(id);
            ra.addAttribute("msg", "리뷰를 복구했습니다.");
        } catch (Exception e) {
            ra.addAttribute("err", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
}

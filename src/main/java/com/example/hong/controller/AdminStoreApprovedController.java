package com.example.hong.controller;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.repository.CafeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStoreApprovedController {

    private final CafeRepository cafeRepository;

    /** 승인된 가게 관리 페이지 */
    @GetMapping("/stores_approved")
    public String approvedList(Model model,
                               @RequestParam(value = "msg", required = false) String msg) {
        var list = cafeRepository
                .findByApprovalStatusOrderByUpdatedAtDesc(ApprovalStatus.APPROVED);
        model.addAttribute("approvedCafes", list);
        if (msg != null) model.addAttribute("msg", msg);
        return "admin/stores_approved";
    }

    /** 노출/숨김 토글 */
    @PostMapping("/stores_approved/cafes/{id}/toggle-visible")
    public String toggleVisible(@PathVariable Long id, RedirectAttributes ra) {
        var cafe = cafeRepository.findById(id).orElseThrow();
        cafe.setVisible(!cafe.isVisible());
        cafeRepository.save(cafe);
        ra.addAttribute("msg", cafe.isVisible() ? "노출로 변경했습니다." : "숨김으로 변경했습니다.");
        return "redirect:/admin/stores_approved";
    }
}

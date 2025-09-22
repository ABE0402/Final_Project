package com.example.hong.controller;

import com.example.hong.service.AdminStoreService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/stores")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStoreController {

    private final AdminStoreService service;

    /* ====== 대기 목록 ====== */
    @GetMapping("/pending")
    public String pending(Model model,
                          @RequestParam(value="msg", required=false) String msg) {
        model.addAttribute("cafes", service.pendingList());
        if (msg != null) model.addAttribute("msg", msg);
        return "admin/stores_pending";
    }

    @GetMapping("/pending/{id}")
    public String review(@PathVariable Long id, Model model,
                         @RequestParam(value="rejected", required=false) String rejected) {
        model.addAttribute("cafe", service.pendingDetail(id));
        if (rejected != null) model.addAttribute("rejected", true);
        return "admin/store_review";
    }

    @PostMapping("/pending/{id}/approve")
    public String approve(@PathVariable Long id, Authentication auth,
                          @RequestParam(required=false) String note) {
        Long adminId = ((AppUserPrincipal) auth.getPrincipal()).getId();
        service.approve(id, adminId, note);
        return "redirect:/admin/stores/pending?msg=승인되었습니다";
    }

    @PostMapping("/pending/{id}/reject")
    public String reject(@PathVariable Long id, Authentication auth,
                         @RequestParam String reason) {
        Long adminId = ((AppUserPrincipal) auth.getPrincipal()).getId();
        service.reject(id, adminId, reason);
        return "redirect:/admin/stores/pending/"+id+"?rejected=1";
    }

}
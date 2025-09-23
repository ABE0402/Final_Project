package com.example.hong.controller;

import com.example.hong.domain.UserRole;
import com.example.hong.repository.UserRepository;
import com.example.hong.service.AdminOwnerApplicationService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/owner-applications")
public class AdminOwnerApplicationController {

    private final AdminOwnerApplicationService service;
    private final UserRepository userRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("apps", service.listPending());
        model.addAttribute("owners",
                userRepository.findAllByRoleOrderByCreatedAtDesc(UserRole.OWNER));
        return "admin/owner_applications";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("app", service.get(id));
        return "admin/owner_app_detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, Authentication auth,
                          @RequestParam(required=false) String note) {
        Long adminId = ((AppUserPrincipal) auth.getPrincipal()).getId();
        service.approve(id, adminId, note);
        return "redirect:/admin/owner-applications";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, Authentication auth,
                         @RequestParam String reason) {
        Long adminId = ((AppUserPrincipal) auth.getPrincipal()).getId();
        service.reject(id, adminId, reason);
        return "redirect:/admin/owner-applications/" + id + "?rejected=1";
    }
}
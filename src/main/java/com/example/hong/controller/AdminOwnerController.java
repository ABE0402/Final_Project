package com.example.hong.controller;

import com.example.hong.domain.UserRole;
import com.example.hong.repository.UserRepository;
import com.example.hong.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/owners")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminOwnerController {

    private final UserRepository userRepository;
    private final AdminUserService adminUserService;

    @GetMapping
    public String listOwners(Model model,
                             @RequestParam(value="msg", required=false) String msg,
                             @RequestParam(value="err", required=false) String err) {
        model.addAttribute("owners",
                userRepository.findAllByRoleOrderByCreatedAtDesc(UserRole.OWNER));
        if (msg != null) model.addAttribute("msg", msg);
        if (err != null) model.addAttribute("err", err);
        return "admin/owners";
    }

    @PostMapping("/{userId}/demote")
    public String demote(@PathVariable Long userId,
                         @RequestParam(required=false) String reason,
                         RedirectAttributes ra) {
        try {
            adminUserService.demoteOwnerToUser(userId, reason);
            ra.addAttribute("msg", "점주 권한을 회수했습니다.");
        } catch (Exception e) {
            ra.addAttribute("err", e.getMessage());
        }
        return "redirect:/admin/owners";
    }
}

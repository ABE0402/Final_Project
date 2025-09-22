package com.example.hong.controller;

import com.example.hong.dto.OwnerApplyRequestDto;
import com.example.hong.service.OwnerApplicationService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/owner")
public class OwnerApplicationController {

    private final OwnerApplicationService ownerApplicationService;

    @GetMapping("/apply")
    public String applyPage() { return "owner/apply"; }

    @PostMapping("/apply")
    public String submit(@ModelAttribute OwnerApplyRequestDto req, Authentication auth, Model model) {
        try {
            Long userId = ((AppUserPrincipal) auth.getPrincipal()).getId();
            ownerApplicationService.submit(userId, req);
            return "redirect:/mypage?applySuccess=1";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "owner/apply";
        }
    }
}

package com.example.hong.controller;

import com.example.hong.domain.AccountStatus;
import com.example.hong.domain.UserRole;
import com.example.hong.entity.User;
import com.example.hong.service.AdminUserService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "status", required = false) String status,
                       @RequestParam(value = "role", required = false) String role,
                       @RequestParam(value = "msg", required = false) String msg,
                       @RequestParam(value = "err", required = false) String err,
                       Model model) {

        String qVal = q == null ? "" : q;
        String statusVal = status == null ? "" : status;
        String roleVal = role == null ? "" : role;

        // 드롭다운 선택 유지용
        model.addAttribute("q", qVal);
        model.addAttribute("status", statusVal);
        model.addAttribute("role", roleVal);
        model.addAttribute("equalsActive", "ACTIVE".equalsIgnoreCase(statusVal));
        model.addAttribute("equalsSuspended", "SUSPENDED".equalsIgnoreCase(statusVal));
        model.addAttribute("equalsUser", "USER".equalsIgnoreCase(roleVal));
        model.addAttribute("equalsOwner", "OWNER".equalsIgnoreCase(roleVal));

        AccountStatus statusFilter =
                "ACTIVE".equalsIgnoreCase(statusVal) ? AccountStatus.ACTIVE :
                        "SUSPENDED".equalsIgnoreCase(statusVal) ? AccountStatus.SUSPENDED : null;
        UserRole roleFilter =
                "USER".equalsIgnoreCase(roleVal) ? UserRole.USER :
                        "OWNER".equalsIgnoreCase(roleVal) ? UserRole.OWNER : null;

        List<User> list = adminUserService.search(qVal, statusFilter, roleFilter);

        // ✅ Mustache가 바로 쓸 수 있도록 플래그/기본값 포함한 맵으로 변환
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User u : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("email", nvl(u.getEmail()));
            m.put("nickname", nvl(u.getNickname()));
            m.put("name", nvl(u.getName()));
            m.put("role", u.getRole() != null ? u.getRole().name() : "-");
            m.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "-");

            boolean isActive = u.getAccountStatus() == AccountStatus.ACTIVE;
            boolean isSuspended = u.getAccountStatus() == AccountStatus.SUSPENDED;
            m.put("accountStatusText", isActive ? "ACTIVE" : isSuspended ? "SUSPENDED" : "-");
            m.put("isActive", isActive);
            m.put("isSuspended", isSuspended);

            // 버튼 노출 조건
            boolean isAdmin = u.getRole() == UserRole.ADMIN;
            m.put("canSuspend", isActive && !isAdmin);
            m.put("canResume", isSuspended); // 필요시 자기 자신/관리자 예외 추가 가능

            rows.add(m);
        }
        model.addAttribute("users", rows);

        if (msg != null) model.addAttribute("msg", msg);
        if (err != null) model.addAttribute("err", err);

        return "admin/users";
    }

    @PostMapping("/{id}/suspend")
    public String suspend(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            Long adminId = ((AppUserPrincipal) auth.getPrincipal()).getId();
            adminUserService.suspend(id, adminId);
            ra.addAttribute("msg", "해당 사용자를 정지했습니다.");
        } catch (Exception e) {
            ra.addAttribute("err", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/resume")
    public String resume(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminUserService.resume(id);
            ra.addAttribute("msg", "해당 사용자를 복구했습니다.");
        } catch (Exception e) {
            ra.addAttribute("err", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    private static String nvl(String s) { return (s == null || s.isBlank()) ? "-" : s; }
}

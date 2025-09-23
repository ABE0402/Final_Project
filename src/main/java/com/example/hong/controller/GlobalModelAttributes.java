package com.example.hong.controller;

import com.example.hong.service.auth.AppUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("auth")
    public Map<String, Object> auth(Authentication authentication) {
        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String); // "anonymousUser" 방지

        String nickname = null;
        String email = null;
        Long userId = null;
        Set<String> roles = Collections.emptySet();

        if (loggedIn) {
            roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            Object principal = authentication.getPrincipal();
            if (principal instanceof AppUserPrincipal p) {
                nickname = p.getNickname();
                email = p.getUsername(); // 이메일
                userId = p.getId();
            } else if (principal instanceof UserDetails ud) {
                email = ud.getUsername();
            } else {
                email = String.valueOf(principal);
            }
        }

        boolean isAdmin = roles.contains("ROLE_ADMIN");
        boolean isOwner = roles.contains("ROLE_OWNER");

        Map<String, Object> model = new HashMap<>();
        model.put("isLoggedIn", loggedIn);
        model.put("isAdmin", isAdmin);   // ✅ 추가
        model.put("isOwner", isOwner);   // (옵션) 필요하면 뷰에서 활용
        if (userId != null) model.put("userId", userId);
        if (nickname != null && !nickname.isBlank()) model.put("nickname", nickname);
        if (email != null && !email.isBlank())       model.put("email", email);
        if (!roles.isEmpty()) model.put("roles", roles); // (옵션) 디버그/표시에 유용
        return model;
    }

    @ModelAttribute("_csrf")
    public Map<String, Object> csrf(CsrfToken token) {
        if (token == null) return Collections.emptyMap();
        Map<String, Object> m = new HashMap<>();
        m.put("parameterName", token.getParameterName());
        m.put("token", token.getToken());
        return m;
    }
}

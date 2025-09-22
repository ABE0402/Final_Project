package com.example.hong.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CsrfModelAdvice {
    @ModelAttribute("_csrf")
    public CsrfToken csrfToken(CsrfToken token) {
        return token; // null이면 머스타치 섹션이 스킵됨
    }
}
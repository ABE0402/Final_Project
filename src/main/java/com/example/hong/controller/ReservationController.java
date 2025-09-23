package com.example.hong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReservationController {

    @GetMapping("/reserve")
    public String showReservationPage() {
        return "reserve";
    }
}

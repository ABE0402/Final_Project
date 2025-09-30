package com.example.hong.domain;

import java.time.LocalDate;
import java.time.Period;

public enum AgeBucket {
    TEENS("10S"), TWENTIES("20S"), THIRTIES("30S"), FORTIES_PLUS("40S+");

    private final String code;

    AgeBucket(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static AgeBucket fromBirthDate(LocalDate birthDate) {
        if (birthDate == null) return null;
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 10) return null;                 // 너무 어리면 제외(집계 X)
        if (age < 20) return TEENS;
        if (age < 30) return TWENTIES;
        if (age < 40) return THIRTIES;
        return FORTIES_PLUS;
    }

}
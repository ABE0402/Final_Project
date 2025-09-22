package com.example.hong.dto;

import com.example.hong.domain.Gender;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phone;
    private Gender gender;
    private LocalDate birthDate;
}
package com.example.hong.dto;


import com.example.hong.domain.Gender;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class ProfileUpdateDto {
    private String nickname;
    private String bio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private Gender gender;          // MALE / FEMALE (enum)

    private MultipartFile photo;    // 프로필 이미지 (선택)
}
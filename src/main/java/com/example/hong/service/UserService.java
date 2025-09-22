package com.example.hong.service;

import com.example.hong.dto.ProfileUpdateDto;
import com.example.hong.dto.SignupRequestDto;
import com.example.hong.entity.User;
import com.example.hong.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private static final String PROFILE_DIR = "profiles";
    private static final String PROFILE_URL_PREFIX = "/uploads/profiles";
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> BLOCKED_EXT = Set.of(
            "svg", "html", "htm", "js", "mjs", "php", "jsp", "asp", "aspx",
            "exe", "sh", "bat", "cmd", "com", "msi", "dll", "ps1", "pl"
    );

    @Transactional
    public boolean changePasswordByEmail(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return true;
    }

    public User signup(SignupRequestDto req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (req.getNickname() == null || req.getNickname().isBlank()) {
            throw new IllegalArgumentException("닉네임을 입력해 주세요.");
        }
        if (userRepository.existsByNickname(req.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .nickname(req.getNickname())
                .phone(req.getPhone())
                .gender(req.getGender())
                .birthDate(req.getBirthDate())
                .build();

        return userRepository.save(user);
    }


    /* ================= 프로필 업데이트 =================
       닉네임/소개/생년월일/성별 + 프로필 이미지 업로드
    */
    @Transactional
    public void updateProfile(Long userId, ProfileUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 닉네임: 변경 요청 시에만 중복 체크
        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            String newNick = dto.getNickname().trim();
            if (!newNick.equals(user.getNickname())) {
                if (userRepository.existsByNickname(newNick)) {
                    throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
                }
                user.setNickname(newNick);
            }
        }

        // 소개 / 생년월일 / 성별
        if (dto.getBio() != null) user.setBio(dto.getBio().trim());
        if (dto.getBirthDate() != null) user.setBirthDate(dto.getBirthDate());
        if (dto.getGender() != null) user.setGender(dto.getGender());

        // 프로필 사진 업로드(선택)
        MultipartFile photo = dto.getPhoto();
        if (photo != null && !photo.isEmpty()) {
            String url = storeProfileImage(photo);
            user.setProfileImageUrl(url);
        }
        // @Transactional로 자동 flush
    }

    /* ================= 이미지 저장 헬퍼 ================= */
    private String storeProfileImage(MultipartFile file) {
        try {
            // 1) 이미지 MIME만 허용 (예: image/jpeg, image/png, image/webp, image/gif 등)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
            }

            // 2) 원본 파일명/확장자 처리
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null) {
                int dot = original.lastIndexOf('.');
                if (dot > -1 && dot < original.length() - 1) {
                    ext = original.substring(dot + 1).toLowerCase();
                }
            }

            // 3) 위험 확장자 차단
            if (!ext.isBlank() && BLOCKED_EXT.contains(ext)) {
                throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다.");
            }

            // 4) 확장자 없으면 contentType으로 추론 (최소 매핑)
            if (ext.isBlank()) {
                switch (contentType.toLowerCase()) {
                    case "image/jpeg" -> ext = "jpg";
                    case "image/png" -> ext = "png";
                    case "image/gif" -> ext = "gif";
                    case "image/webp" -> ext = "webp"; // JDK 기본 디코딩 미지원일 수는 있지만 저장/서빙은 가능
                    default -> ext = "png";            // 모르면 png로 저장
                }
            }

            // 5) 저장
            String filename = UUID.randomUUID() + "." + ext;
            Path dir = Paths.get(uploadDir, PROFILE_DIR).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            log.info("[PROFILE] saved: {}", target.toAbsolutePath());
            return PROFILE_URL_PREFIX + "/" + filename; // 브라우저 접근 URL
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("프로필 이미지를 저장하지 못했습니다.", e);
        }
    }
}
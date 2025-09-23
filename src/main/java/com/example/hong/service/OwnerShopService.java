package com.example.hong.service;

import com.example.hong.domain.UserRole;
import com.example.hong.dto.ShopCreateRequestDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.User;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerShopService {

    private final UserRepository userRepository;
    private final CafeRepository cafeRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadRoot;

    private static final String CAFE_DIR = "cafes";
    private static final String URL_PREFIX = "/uploads/cafes";
    private static final Set<String> ALLOWED_EXT = Set.of("jpg","jpeg","png","gif","webp","bmp");

    /** OWNER만 가게 등록 가능 */
    @Transactional
    public Long createShop(Long ownerUserId, ShopCreateRequestDto req) {
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (owner.getRole() != UserRole.OWNER) {
            throw new AccessDeniedException("점주(OWNER)만 등록할 수 있습니다.");
        }

        if ("CAFE".equalsIgnoreCase(req.getType())) {
            return createCafe(owner, req);
        } else if ("RESTAURANT".equalsIgnoreCase(req.getType())) {
            // TODO: Restaurant 등록은 추후 구현
            throw new UnsupportedOperationException("맛집 등록은 추후 구현 예정입니다.");
        } else {
            throw new IllegalArgumentException("유형(type)을 선택해 주세요.");
        }
    }

    /** 카페 생성 */
    private Long createCafe(User owner, ShopCreateRequestDto req) {
        // 좌표는 지금 단계에선 선택(없어도 됨)
        BigDecimal lat = null, lng = null;
        if (req.getLat() != null && req.getLng() != null) {
            double dlat = req.getLat(), dlng = req.getLng();
            if (dlat < -90 || dlat > 90 || dlng < -180 || dlng > 180) {
                throw new IllegalArgumentException("좌표 범위가 올바르지 않습니다.");
            }
            lat = BigDecimal.valueOf(dlat);
            lng = BigDecimal.valueOf(dlng);
        }

        Cafe c = Cafe.builder()
                .owner(owner)
                .name(req.getName())
                .phone(emptyToNull(req.getPhone()))
                .addressRoad(req.getAddressRoad())
                .postcode(emptyToNull(req.getPostcode()))
                .description(emptyToNull(req.getDescription()))
                .lat(lat)  // null 가능
                .lng(lng)  // null 가능
                .build();

        if (req.getImage() != null && !req.getImage().isEmpty()) {
            c.setHeroImageUrl(storeHero(req.getImage()));
        }
        return cafeRepository.save(c).getId();
    }
    /** 대표 이미지 저장 */
    private String storeHero(MultipartFile file) {
        try {
            String orig = file.getOriginalFilename();
            String ext = (orig != null && orig.contains(".")) ?
                    orig.substring(orig.lastIndexOf('.') + 1).toLowerCase() : "";
            if (!ALLOWED_EXT.contains(ext)) {
                throw new IllegalArgumentException("허용되지 않은 이미지 형식: " + ext);
            }
            String filename = UUID.randomUUID() + "." + ext;
            Path dir = Paths.get(uploadRoot, CAFE_DIR);
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return URL_PREFIX + "/" + filename;
        } catch (Exception e) {
            log.error("storeHero failed", e);
            throw new IllegalStateException("대표 이미지를 저장하지 못했습니다.");
        }
    }

    /** 수정 화면용 데이터 (간단 Map으로 반환) */
    @Transactional
    public Map<String, Object> getShopForEdit(Long ownerId, Long shopId) {
        Cafe c = cafeRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));
        verifyOwner(c, ownerId);

        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("type", "CAFE");
        m.put("name", c.getName());
        m.put("phone", c.getPhone());
        m.put("address", c.getAddressRoad());
        m.put("postcode", c.getPostcode());
        m.put("description", c.getDescription());
        m.put("lat", c.getLat() != null ? c.getLat().toPlainString() : "");
        m.put("lng", c.getLng() != null ? c.getLng().toPlainString() : "");
        m.put("heroImageUrl", c.getHeroImageUrl());
        return m;
    }

    /** 가게 정보 수정 */
    @Transactional
    public void updateShop(Long ownerId, Long shopId, ShopCreateRequestDto req) {
        Cafe c = cafeRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));
        verifyOwner(c, ownerId);

        if (req.getName() != null && !req.getName().isBlank()) c.setName(req.getName().trim());
        c.setPhone(emptyToNull(req.getPhone()));
        c.setAddressRoad(emptyToNull(req.getAddressRoad()));
        c.setPostcode(emptyToNull(req.getPostcode()));
        c.setDescription(emptyToNull(req.getDescription()));

        if (req.getLat() != null) c.setLat(BigDecimal.valueOf(req.getLat()));
        if (req.getLng() != null) c.setLng(BigDecimal.valueOf(req.getLng()));

        if (req.getImage() != null && !req.getImage().isEmpty()) {
            c.setHeroImageUrl(storeHero(req.getImage()));
        }
        // 승인 상태/가시성 변경은 관리자 프로세스에서 처리한다고 가정
    }

    /** 폐업(숨김) 처리 */
    @Transactional
    public void requestClose(Long ownerId, Long shopId) {
        Cafe c = cafeRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));
        verifyOwner(c, ownerId);
        c.setVisible(false);
    }

    /* ================== 유틸 ================== */
    private void verifyOwner(Cafe c, Long ownerId) {
        if (c.getOwner() == null || c.getOwner().getId() == null || !Objects.equals(c.getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("본인 매장만 처리할 수 있습니다.");
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}

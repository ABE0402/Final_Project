// src/main/java/com/example/hong/service/OwnerShopService.java
package com.example.hong.service;

import com.example.hong.domain.UserRole;
import com.example.hong.dto.ShopCreateRequestDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.CafeTag;
import com.example.hong.entity.Tag;
import com.example.hong.entity.User;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.CafeTagRepository;
import com.example.hong.repository.TagRepository;
import com.example.hong.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class OwnerShopService {

    private final UserRepository userRepository;
    private final CafeRepository cafeRepository;
    private final TagRepository tagRepository;
    private final CafeTagRepository cafeTagRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadRoot;

    private static final String CAFE_DIR = "cafes";
    private static final String MENU_DIR = "menus";
    private static final String URL_PREFIX_CAFE = "/uploads/cafes";
    private static final String URL_PREFIX_MENU = "/uploads/menus";
    private static final Set<String> ALLOWED_EXT = Set.of("jpg","jpeg","png","gif","webp","bmp");

    /** 카테고리별 선택 제한 */
    private static final Map<String, Integer> CATEGORY_LIMITS = Map.of(
            "companion",   2,
            "mood",        2,
            "amenities",   2,
            "reservation", 1, // ★ 예약여부는 1개만
            "priority",    2,
            "type",        2
    );

    @Transactional
    public Long createShop(Long ownerUserId, ShopCreateRequestDto req) {
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (owner.getRole() != UserRole.OWNER)
            throw new AccessDeniedException("점주(OWNER)만 등록할 수 있습니다.");

        if (!"CAFE".equalsIgnoreCase(req.getType()))
            throw new IllegalArgumentException("현재는 CAFE만 등록을 지원합니다.");

        return createCafe(owner, req);
    }

    private Long createCafe(User owner, ShopCreateRequestDto req) {
        BigDecimal lat = req.getLat() == null ? null : BigDecimal.valueOf(req.getLat());
        BigDecimal lng = req.getLng() == null ? null : BigDecimal.valueOf(req.getLng());

        Cafe c = Cafe.builder()
                .owner(owner)
                .name(req.getName())
                .phone(emptyToNull(req.getPhone()))
                .addressRoad(req.getAddressRoad())
                .postcode(emptyToNull(req.getPostcode()))
                .description(emptyToNull(req.getDescription()))
                .lat(lat)
                .lng(lng)
                .build();

        // 사업자번호
        c.setBusinessNumber(emptyToNull(req.getBusinessNumber()));

        // 대표 이미지
        if (req.getImage() != null && !req.getImage().isEmpty()) {
            c.setHeroImageUrl(storeImage(req.getImage(), CAFE_DIR, URL_PREFIX_CAFE));
        }

        // 운영시간/메뉴 텍스트
        c.setOperatingHours(nvl(req.getOperatingHours()));
        c.setMenuText(nvl(req.getMenuText()));

        // 메뉴 이미지(최대 5장)
        applyMenuImages(c, req.getMenuImages());

        cafeRepository.save(c);

        // 태그
        attachTags(c, req.getTagIds());
        return c.getId();
    }

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
        m.put("businessNumber", c.getBusinessNumber());
        m.put("postcode", c.getPostcode());
        m.put("addressRoad", c.getAddressRoad());
        m.put("description", c.getDescription());
        m.put("lat", c.getLat() != null ? c.getLat().toPlainString() : "");
        m.put("lng", c.getLng() != null ? c.getLng().toPlainString() : "");
        m.put("heroImageUrl", c.getHeroImageUrl());
        m.put("operatingHours", nvl(c.getOperatingHours()));
        m.put("menuText", nvl(c.getMenuText()));

        List<String> menuImgs = new ArrayList<>();
        if (c.getMenuImageUrl1() != null) menuImgs.add(c.getMenuImageUrl1());
        if (c.getMenuImageUrl2() != null) menuImgs.add(c.getMenuImageUrl2());
        if (c.getMenuImageUrl3() != null) menuImgs.add(c.getMenuImageUrl3());
        if (c.getMenuImageUrl4() != null) menuImgs.add(c.getMenuImageUrl4());
        if (c.getMenuImageUrl5() != null) menuImgs.add(c.getMenuImageUrl5());
        m.put("menuImages", menuImgs);

        var selected = cafeTagRepository.findByCafe(c).stream()
                .map(ct -> ct.getTag().getId())
                .toList();
        m.put("selectedTagIds", selected);
        return m;
    }

    @Transactional
    public void reopen(Long ownerId, Long shopId) {
        Cafe c = cafeRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));
        verifyOwner(c, ownerId);
        c.setVisible(true);
    }


    @Transactional
    public void updateShop(Long ownerId, Long shopId, ShopCreateRequestDto req) {
        Cafe c = cafeRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));
        verifyOwner(c, ownerId);

        if (req.getName() != null && !req.getName().isBlank()) c.setName(req.getName().trim());
        c.setPhone(emptyToNull(req.getPhone()));
        c.setBusinessNumber(emptyToNull(req.getBusinessNumber()));
        c.setPostcode(emptyToNull(req.getPostcode()));
        c.setAddressRoad(emptyToNull(req.getAddressRoad()));
        c.setDescription(emptyToNull(req.getDescription()));

        if (req.getLat() != null) c.setLat(BigDecimal.valueOf(req.getLat()));
        if (req.getLng() != null) c.setLng(BigDecimal.valueOf(req.getLng()));

        if (req.getImage() != null && !req.getImage().isEmpty()) {
            c.setHeroImageUrl(storeImage(req.getImage(), CAFE_DIR, URL_PREFIX_CAFE));
        }

        // 운영시간/메뉴 텍스트 갱신
        if (req.getOperatingHours() != null) c.setOperatingHours(req.getOperatingHours());
        if (req.getMenuText() != null)      c.setMenuText(req.getMenuText());

        // 메뉴 이미지 새 업로드가 있으면 교체
        if (req.getMenuImages() != null && req.getMenuImages().stream().anyMatch(f -> f != null && !f.isEmpty())) {
            clearMenuImages(c);
            applyMenuImages(c, req.getMenuImages());
        }

        // 태그 교체
        replaceTags(c, req.getTagIds());
    }

    @Transactional
    public void requestClose(Long ownerId, Long shopId) {
        Cafe c = cafeRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));
        verifyOwner(c, ownerId);
        c.setVisible(false);
    }

    /* ================= 이미지 유틸 ================= */

    private String storeImage(MultipartFile file, String dirName, String urlPrefix) {
        try {
            String orig = file.getOriginalFilename();
            String ext = (orig != null && orig.contains(".")) ?
                    orig.substring(orig.lastIndexOf('.') + 1).toLowerCase() : "";
            if (!ALLOWED_EXT.contains(ext)) {
                throw new IllegalArgumentException("허용되지 않은 이미지 형식: " + ext);
            }
            String filename = UUID.randomUUID() + "." + ext;
            Path dir = Paths.get(uploadRoot, dirName);
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            // 정적 자원 매핑이 /uploads/** → {uploadRoot} 라고 가정
            return ("/uploads/" + dirName + "/" + filename).replace("\\","/");
        } catch (Exception e) {
            log.error("storeImage failed", e);
            throw new IllegalStateException("이미지를 저장하지 못했습니다.");
        }
    }

    private void applyMenuImages(Cafe c, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return;
        List<String> urls = new ArrayList<>();
        for (MultipartFile f : images) {
            if (f == null || f.isEmpty()) continue;
            if (urls.size() >= 5) break;
            urls.add(storeImage(f, MENU_DIR, URL_PREFIX_MENU));
        }
        if (urls.size() > 0) c.setMenuImageUrl1(urls.get(0));
        if (urls.size() > 1) c.setMenuImageUrl2(urls.get(1));
        if (urls.size() > 2) c.setMenuImageUrl3(urls.get(2));
        if (urls.size() > 3) c.setMenuImageUrl4(urls.get(3));
        if (urls.size() > 4) c.setMenuImageUrl5(urls.get(4));
    }

    private void clearMenuImages(Cafe c) {
        c.setMenuImageUrl1(null);
        c.setMenuImageUrl2(null);
        c.setMenuImageUrl3(null);
        c.setMenuImageUrl4(null);
        c.setMenuImageUrl5(null);
    }

    /* ================= 태그 유틸 ================= */

    private void attachTags(Cafe cafe, List<Integer> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        Set<Integer> uniq = new LinkedHashSet<>(tagIds);
        List<Tag> tags = tagRepository.findByIdIn(uniq);
        validateCategoryLimits(tags);
        for (Tag t : tags) {
            if (!uniq.contains(t.getId())) continue;
            cafeTagRepository.save(CafeTag.of(cafe, t));
        }
    }

    private void replaceTags(Cafe cafe, List<Integer> tagIds) {
        cafeTagRepository.deleteByCafe(cafe);
        attachTags(cafe, tagIds);
    }

    private void validateCategoryLimits(List<Tag> tags) {
        var counts = tags.stream().collect(Collectors.groupingBy(Tag::getCategory, Collectors.counting()));
        counts.forEach((cat, cnt) -> {
            int limit = CATEGORY_LIMITS.getOrDefault(cat, 2);
            if (cnt > limit) {
                throw new IllegalArgumentException(cat + " 태그는 최대 " + limit + "개까지 선택 가능합니다.");
            }
        });
    }

    /* ================= 공통 ================= */

    private void verifyOwner(Cafe c, Long ownerId) {
        if (c.getOwner() == null || c.getOwner().getId() == null || !Objects.equals(c.getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("본인 매장만 처리할 수 있습니다.");
        }
    }
    private String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private String nvl(String s) { return (s == null) ? "" : s; }
}

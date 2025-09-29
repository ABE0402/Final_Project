package com.example.hong.controller;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Tag;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.CafeTagRepository;
import com.example.hong.service.FavoriteService;
import com.example.hong.service.ReviewService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CafeController {

    private final CafeRepository cafeRepository;
    private final CafeTagRepository cafeTagRepository; // ✅ 추가
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;

    private Long meId(Authentication a) {
        return (a != null && a.isAuthenticated() && !(a.getPrincipal() instanceof String))
                ? ((AppUserPrincipal) a.getPrincipal()).getId()
                : null;
    }

    @GetMapping("/cafes/{id}")
    public String show(@PathVariable Long id,
                       Authentication auth,
                       Model model,
                       @RequestParam(value = "msg", required = false) String msg,
                       @RequestParam(value = "err", required = false) String err) {

        Cafe c = cafeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // ====== 이미지 레일(히어로 보조 썸네일) ======
        List<String> thumbs = new ArrayList<>();
        // 대표사진이 있으면 최우선
        if (notBlank(c.getHeroImageUrl())) thumbs.add(c.getHeroImageUrl());
        // 메뉴 사진도 썸네일 레일에 섞어서 최대 3장까지
        if (notBlank(c.getMenuImageUrl1())) thumbs.add(c.getMenuImageUrl1());
        if (notBlank(c.getMenuImageUrl2())) thumbs.add(c.getMenuImageUrl2());
        if (notBlank(c.getMenuImageUrl3())) thumbs.add(c.getMenuImageUrl3());
        // 대체 이미지
        if (thumbs.isEmpty()) {
            thumbs = List.of(
                    "/images/placeholder_shop.jpg",
                    "/images/placeholder_shop.jpg",
                    "/images/placeholder_shop.jpg"
            );
        } else if (thumbs.size() == 1) {
            // 1장만 있으면 레일 느낌을 위해 복제
            thumbs = List.of(thumbs.get(0), thumbs.get(0), thumbs.get(0));
        } else if (thumbs.size() == 2) {
            thumbs = List.of(thumbs.get(0), thumbs.get(1), thumbs.get(0));
        } else if (thumbs.size() > 3) {
            thumbs = thumbs.subList(0, 3);
        }
        // show.mustache가 2중 반복을 쓰므로 List<List<String>> 형태로 전달
        model.addAttribute("images", List.of(thumbs));

        // ====== 사이드 베스트 ======
        List<Cafe> best = cafeRepository
                .findTop8ByApprovalStatusAndIsVisibleOrderByAverageRatingDescReviewCountDesc(
                        ApprovalStatus.APPROVED, true);
        model.addAttribute("bestCafes", best);

        // ====== 기본 정보 ======
        model.addAttribute("cafe", c);
        model.addAttribute("avg", c.getAverageRating() == null ? "0.0" : c.getAverageRating().toPlainString());
        model.addAttribute("hasGeo", c.getLat() != null && c.getLng() != null);

        // ====== 메뉴(텍스트/사진) ======
        // menuText는 템플릿에서 {{#cafe}}{{#menuText}}… 으로 직접 접근
        List<String> menuImgs = new ArrayList<>();
        if (notBlank(c.getMenuImageUrl1())) menuImgs.add(c.getMenuImageUrl1());
        if (notBlank(c.getMenuImageUrl2())) menuImgs.add(c.getMenuImageUrl2());
        if (notBlank(c.getMenuImageUrl3())) menuImgs.add(c.getMenuImageUrl3());
        if (notBlank(c.getMenuImageUrl4())) menuImgs.add(c.getMenuImageUrl4());
        if (notBlank(c.getMenuImageUrl5())) menuImgs.add(c.getMenuImageUrl5());
        model.addAttribute("menuImages", menuImgs.isEmpty() ? null : List.of(menuImgs)); // 2중 반복 대응

        // ====== 태그(칩/해시태그/카테고리별) ======
        List<Tag> tagEntities = cafeTagRepository.findTagsByCafeId(id);
        List<String> tagNames = tagEntities.stream().map(Tag::getName).toList();
        model.addAttribute("tagsAll", tagNames); // 칩 나열용

        String hashtags = tagNames.stream()
                .map(n -> "#" + n.replaceAll("\\s+", "")) // 공백 제거
                .collect(Collectors.joining(" "));
        model.addAttribute("hashtags", hashtags.isBlank() ? null : hashtags); // 히어로 아래 해시태그 한 줄

        Map<String, List<String>> byCat = tagEntities.stream()
                .collect(Collectors.groupingBy(
                        Tag::getCategory,
                        LinkedHashMap::new,
                        Collectors.mapping(Tag::getName, Collectors.toList())
                ));
        model.addAttribute("tags", byCat); // tags.companion / tags.mood / tags.amenities / tags.reservation / tags.type

        // ====== 리뷰 ======
        model.addAttribute("reviews", reviewService.listForCafeDtos(id));

        // ====== 즐겨찾기 ======
        Long uid = meId(auth);
        boolean isFav = (uid != null) && favoriteService.isFavorite(uid, id);
        model.addAttribute("isFav", isFav);

        // ====== 메시지 ======
        if (msg != null) model.addAttribute("msg", msg);
        if (err != null) model.addAttribute("err", err);

        return "cafes/show";
    }

    @GetMapping("/cafes/{id}/reviews/new")
    public String newReview(@PathVariable Long id, Model model) {
        Cafe c = cafeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("cafe", c);
        return "cafes/reviews_new";
    }

    @PostMapping(value = "/cafes/{id}/reviews", consumes = "multipart/form-data")
    public String createReview(@PathVariable Long id,
                               Authentication auth,
                               @RequestParam int rating,
                               @RequestParam String content,
                               @RequestParam(name = "images", required = false) List<MultipartFile> images,
                               RedirectAttributes ra) {
        try {
            Long uid = meId(auth);
            if (uid == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            reviewService.addWithImages(uid, id, rating, content, images);
            ra.addAttribute("msg", "리뷰가 등록되었습니다.");
        } catch (Exception e) {
            ra.addAttribute("err", e.getMessage());
        }
        return "redirect:/cafes/{id}";
    }

    @PostMapping("/cafes/{id}/favorite")
    public String toggleFavorite(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        Long uid = meId(auth);
        if (uid == null) {
            ra.addFlashAttribute("err", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        cafeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        favoriteService.toggle(uid, id);
        return "redirect:/cafes/" + id;
    }

    /* ===== helpers ===== */
    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
}

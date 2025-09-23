// src/main/java/com/example/HONGAROUND/controller/CafeController.java
package com.example.hong.controller;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.entity.Cafe;
import com.example.hong.repository.CafeRepository;
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

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CafeController {

    private final CafeRepository cafeRepository;
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

        // 상단 썸네일 3칸
        List<String> images = (c.getHeroImageUrl() != null && !c.getHeroImageUrl().isBlank())
                ? List.of(c.getHeroImageUrl(), c.getHeroImageUrl(), c.getHeroImageUrl())
                : List.of("/images/placeholder_shop.jpg",
                "/images/placeholder_shop.jpg",
                "/images/placeholder_shop.jpg");

        // 우측 베스트 카페
        List<Cafe> best = cafeRepository
                .findTop8ByApprovalStatusAndIsVisibleOrderByAverageRatingDescReviewCountDesc(
                        ApprovalStatus.APPROVED, true);

        // 기본 정보
        model.addAttribute("cafe", c);
        model.addAttribute("images", images);
        model.addAttribute("avg", c.getAverageRating() == null ? "0.0" : c.getAverageRating().toPlainString());
        model.addAttribute("hasGeo", c.getLat() != null && c.getLng() != null);

        // 메뉴/리뷰
        model.addAttribute("menus", Collections.emptyList());        // TODO: 메뉴 연동 시 교체
        model.addAttribute("reviews", reviewService.listForCafeDtos(id)); // ★ nickname 포함되도록 서비스에서 변환

        // 즐겨찾기 여부
        Long uid = meId(auth);
        boolean isFav = false;
        if (uid != null) {
            isFav = favoriteService.isFavorite(uid, id);
        }
        model.addAttribute("isFav", isFav);

        // 알림 메시지
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

        // 카페 존재 확인
        cafeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 즐겨찾기 토글
        favoriteService.toggle(uid, id);

        // 상세 페이지로 리다이렉트 (GET에서 isFav 재계산)
        return "redirect:/cafes/" + id;
    }
}

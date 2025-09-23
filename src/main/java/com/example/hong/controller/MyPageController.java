package com.example.hong.controller;

import com.example.hong.dto.ProfileUpdateDto;
import com.example.hong.entity.User;
import com.example.hong.repository.ReviewRepository;
import com.example.hong.repository.UserRepository;
import com.example.hong.service.FavoriteService;
import com.example.hong.service.OwnerApplicationService;
import com.example.hong.service.ReviewService;
import com.example.hong.service.UserService;
import com.example.hong.service.auth.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final FavoriteService favoriteService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final OwnerApplicationService ownerAppService;

    /* ---------- 공통 유틸 ---------- */
    private Long meId(Authentication auth) { return ((AppUserPrincipal) auth.getPrincipal()).getId(); }
    private void putMe(Authentication auth, Model model) {
        User me = userRepository.findById(meId(auth)).orElseThrow();
        model.addAttribute("me", me);
        model.addAttribute("genderMale",  me.getGender()!=null && me.getGender().name().equals("MALE"));
        model.addAttribute("genderFemale",me.getGender()!=null && me.getGender().name().equals("FEMALE"));
    }
    private void activateTab(Model model, String tab) {
        Map<String, Boolean> t = new HashMap<>();
        t.put("profile", "profile".equals(tab));
        t.put("reviews", "reviews".equals(tab));
        t.put("reservations", "reservations".equals(tab));
        t.put("favorites", "favorites".equals(tab));
        t.put("account", "account".equals(tab));
        model.addAttribute("tab", t);
    }

    /* ---------- 1) 프로필관리 (기존) ---------- */
    @GetMapping
    public String profile(Authentication auth,
                          @RequestParam(value="saved", required=false) String saved,
                          Model model) {
        putMe(auth, model);
        activateTab(model, "profile");
        if (saved != null) model.addAttribute("saved", true);
        return "mypage/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication auth, @ModelAttribute ProfileUpdateDto dto, Model model) {
        try {
            userService.updateProfile(meId(auth), dto);
            return "redirect:/mypage?saved=1";
        } catch (Exception e) {
            putMe(auth, model);
            activateTab(model, "profile");
            model.addAttribute("errorMessage", e.getMessage());
            return "mypage/profile";
        }
    }

    /* ---------- 2) 리뷰관리 ---------- */
    @GetMapping("/reviews")
    public String myReviews(Authentication auth, Model model) {
        putMe(auth, model);
        activateTab(model, "reviews");

        Long uid = ((AppUserPrincipal) auth.getPrincipal()).getId();
        model.addAttribute("reviews", reviewService.myReviews(uid)); // ✅ ReviewItemDto 리스트 주입
        return "mypage/reviews";
    }

    @GetMapping("/reviews/{id}/edit")
    public String editReviewForm(@PathVariable Long id, Authentication auth, Model model) {
        putMe(auth, model); activateTab(model, "reviews");
        var r = reviewRepository.findByIdAndUserIdAndDeletedFalse(id, meId(auth)).orElseThrow();
        model.addAttribute("review", r);
        return "mypage/review_edit";
    }

    @PostMapping("/reviews/{id}/edit")
    public String editReview(@PathVariable Long id,
                             @RequestParam Integer rating,
                             @RequestParam String content,
                             Authentication auth, RedirectAttributes ra) {
        reviewService.updateMyReview(meId(auth), id, content, rating);
        ra.addFlashAttribute("msg", "리뷰가 수정되었습니다.");
        return "redirect:/mypage/reviews";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        reviewService.deleteMyReview(meId(auth), id);
        ra.addFlashAttribute("msg", "리뷰가 삭제되었습니다.");
        return "redirect:/mypage/reviews";
    }

    /* ---------- 3) 예약내역 ---------- */
    @GetMapping("/reservations")
    public String myReservations(Authentication auth, Model model) {
        putMe(auth, model);
        activateTab(model, "reservations");
        model.addAttribute("reservations", Collections.emptyList());
        return "mypage/reservations";
    }

    /* ---------- 4) 즐겨찾기 ---------- */
    @GetMapping("/favorites")
    public String myFavorites(Authentication auth,
                              @RequestParam(value="msg", required=false) String msg,
                              Model model) {
        Long uid = ((AppUserPrincipal) auth.getPrincipal()).getId();
        var items = favoriteService.myFavoritesDto(uid);

        // 탭 활성 + 공통 me 주입 유틸 있으면 호출
        // putMe(auth, model);
        activateTab(model, "favorites");

        model.addAttribute("favorites", items);
        if (msg != null) model.addAttribute("msg", msg);
        return "mypage/favorites";
    }

    @PostMapping("/favorites/{cafeId}/remove")
    public String removeFavorite(@PathVariable Long cafeId,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        Long uid = ((AppUserPrincipal) auth.getPrincipal()).getId();
        favoriteService.remove(uid, cafeId);
        ra.addAttribute("msg", "즐겨찾기에서 제거했습니다.");
        return "redirect:/mypage/favorites";
    }

    /* ---------- 5) 계정관리 (점주 신청 노출) ---------- */
    @GetMapping("/account")
    public String myAccount(Authentication auth,
                            @RequestParam(value="pwChanged", required=false) String pwChanged,
                            @RequestParam(value="pwError", required=false) String pwError,
                            Model model) {
        putMe(auth, model);
        activateTab(model, "account");
        model.addAttribute("apps", ownerAppService.myApplications(meId(auth))); // 점주 신청 현황
        if (pwChanged != null) model.addAttribute("pwChanged", true);
        if (pwError != null)   model.addAttribute("pwError", pwError);
        return "mypage/account";
    }

    @PostMapping("/account/password")
    public String changePassword(Authentication auth,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword) {
        if (!Objects.equals(newPassword, confirmPassword)) {
            return "redirect:/mypage/account?pwError=비밀번호 확인이 일치하지 않습니다.";
        }
        String email = userRepository.findById(meId(auth)).orElseThrow().getEmail();
        boolean ok = userService.changePasswordByEmail(email, currentPassword, newPassword);
        if (!ok) {
            return "redirect:/mypage/account?pwError=현재 비밀번호가 올바르지 않습니다.";
        }
        return "redirect:/mypage/account?pwChanged=1";
    }
}

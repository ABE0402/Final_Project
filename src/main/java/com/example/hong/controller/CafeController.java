// src/main/java/com/example/hong/controller/CafeController.java
package com.example.hong.controller;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Tag;
import com.example.hong.entity.UserEvent;                 // ★ 추가
import com.example.hong.repository.*;
import com.example.hong.service.FavoriteService;
import com.example.hong.service.ReviewService;
import com.example.hong.service.SegmentRealtimeService;  // ★ 추가
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CafeController {

    private final CafeRepository cafeRepository;
    private final CafeTagRepository cafeTagRepository;
    private final ReviewService reviewService;
    private final FavoriteService favoriteService;
    private final OwnerReplyRepository ownerReplyRepository;

    // ★ 이벤트 적재에 필요한 의존성
    private final UserRepository userRepository;               // ★ 추가
    private final UserEventRepository userEventRepository;     // ★ 추가
    private final SegmentRealtimeService segmentRealtimeService; // ★ 추가

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

        // ✅ 상세 진입 클릭 이벤트 (로그인 사용자만)
        Long uid = meId(auth);
        if (uid != null) {
            try {
                var user = userRepository.getReferenceById(uid);
                var ev = userEventRepository.save(UserEvent.click(user, c));
                segmentRealtimeService.apply(ev); // 즉시 반영
            } catch (Exception ignore) {}
        }

        // ====== 이하 기존 코드 유지 ======
        List<String> thumbs = new ArrayList<>();
        if (notBlank(c.getHeroImageUrl())) thumbs.add(c.getHeroImageUrl());
        if (notBlank(c.getMenuImageUrl1())) thumbs.add(c.getMenuImageUrl1());
        if (notBlank(c.getMenuImageUrl2())) thumbs.add(c.getMenuImageUrl2());
        if (notBlank(c.getMenuImageUrl3())) thumbs.add(c.getMenuImageUrl3());
        if (thumbs.isEmpty()) {
            thumbs = List.of("/images/placeholder_shop.jpg","/images/placeholder_shop.jpg","/images/placeholder_shop.jpg");
        } else if (thumbs.size() == 1) {
            thumbs = List.of(thumbs.get(0), thumbs.get(0), thumbs.get(0));
        } else if (thumbs.size() == 2) {
            thumbs = List.of(thumbs.get(0), thumbs.get(1), thumbs.get(0));
        } else if (thumbs.size() > 3) {
            thumbs = thumbs.subList(0, 3);
        }
        model.addAttribute("images", List.of(thumbs));

        List<Cafe> best = cafeRepository
                .findTop8ByApprovalStatusAndIsVisibleOrderByAverageRatingDescReviewCountDesc(
                        ApprovalStatus.APPROVED, true);
        model.addAttribute("bestCafes", best);

        model.addAttribute("cafe", c);
        model.addAttribute("avg", c.getAverageRating() == null ? "0.0" : c.getAverageRating().toPlainString());
        model.addAttribute("hasGeo", c.getLat() != null && c.getLng() != null);

        List<String> menuImgs = new ArrayList<>();
        if (notBlank(c.getMenuImageUrl1())) menuImgs.add(c.getMenuImageUrl1());
        if (notBlank(c.getMenuImageUrl2())) menuImgs.add(c.getMenuImageUrl2());
        if (notBlank(c.getMenuImageUrl3())) menuImgs.add(c.getMenuImageUrl3());
        if (notBlank(c.getMenuImageUrl4())) menuImgs.add(c.getMenuImageUrl4());
        if (notBlank(c.getMenuImageUrl5())) menuImgs.add(c.getMenuImageUrl5());
        model.addAttribute("menuImages", menuImgs.isEmpty() ? null : List.of(menuImgs));

        List<Tag> tagEntities = cafeTagRepository.findTagsByCafeId(id);
        List<String> tagNames = tagEntities.stream().map(Tag::getName).collect(Collectors.toList());
        model.addAttribute("tagsAll", tagNames);

        String hashtags = tagNames.stream()
                .map(n -> "#" + n.replaceAll("\\s+", ""))
                .collect(Collectors.joining(" "));
        model.addAttribute("hashtags", hashtags.isBlank() ? null : hashtags);

        Map<String, List<String>> byCat = tagEntities.stream()
                .collect(Collectors.groupingBy(
                        Tag::getCategory,
                        LinkedHashMap::new,
                        Collectors.mapping(Tag::getName, Collectors.toList())
                ));
        model.addAttribute("tags", byCat);

        var reviewDtos = reviewService.listForCafeDtos(id);
        if (reviewDtos == null || reviewDtos.isEmpty()) {
            model.addAttribute("reviews", Collections.emptyList());
        } else {
            List<Long> reviewIds = reviewDtos.stream()
                    .map(dto -> {
                        try { return (Long) invoke(dto, "getId"); } catch (Exception e) { return null; }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            var replies = ownerReplyRepository.findByReview_IdIn(reviewIds);
            Map<Long, com.example.hong.entity.OwnerReply> replyMap = replies.stream()
                    .collect(Collectors.toMap(
                            or -> or.getReview().getId(),
                            or -> or,
                            (a,b)->{
                                var ta = a.getUpdatedAt()!=null? a.getUpdatedAt():a.getCreatedAt();
                                var tb = b.getUpdatedAt()!=null? b.getUpdatedAt():b.getCreatedAt();
                                if (ta==null) return b; if (tb==null) return a;
                                return ta.isAfter(tb)? a:b;
                            }
                    ));
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            List<Map<String,Object>> viewReviews = new ArrayList<>();
            for (Object dto : reviewDtos) {
                Map<String,Object> m = new HashMap<>();
                try {
                    m.put("id",         invoke(dto, "getId"));
                    m.put("rating",     invoke(dto, "getRating"));
                    m.put("createdAt",  invokeStrOr(dto, "getCreatedAt", fmt));
                    m.put("nickname",   invoke(dto, "getNickname"));
                    m.put("content",    invoke(dto, "getContent"));
                    m.put("imageUrl1",  invoke(dto, "getImageUrl1"));
                    m.put("imageUrl2",  invoke(dto, "getImageUrl2"));
                    m.put("imageUrl3",  invoke(dto, "getImageUrl3"));
                    m.put("imageUrl4",  invoke(dto, "getImageUrl4"));
                    m.put("imageUrl5",  invoke(dto, "getImageUrl5"));
                } catch (Exception ignored) {}
                Long rid = (Long)m.get("id");
                var rep = (rid!=null)? replyMap.get(rid) : null;
                if (rep != null) {
                    var ts = rep.getUpdatedAt()!=null? rep.getUpdatedAt(): rep.getCreatedAt();
                    String uts = (ts!=null)? ts.format(fmt) : null;
                    Map<String,Object> ownerReplyVm = new HashMap<>();
                    ownerReplyVm.put("content", rep.getContent());
                    if (uts!=null) ownerReplyVm.put("updatedAt", uts);
                    m.put("ownerReply", ownerReplyVm);
                    m.put("replyContent", rep.getContent());
                    if (uts!=null) m.put("replyUpdatedAt", uts);
                }
                viewReviews.add(m);
            }
            model.addAttribute("reviews", viewReviews);
        }

        boolean isFav = (uid != null) && favoriteService.isFavorite(uid, id);
        model.addAttribute("isFav", isFav);

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

            // ✅ 리뷰 이벤트
            try {
                var user = userRepository.getReferenceById(uid);
                var cafe = cafeRepository.getReferenceById(id);
                var ev = userEventRepository.save(UserEvent.review(user, cafe, rating));
                segmentRealtimeService.apply(ev);
            } catch (Exception ignore) {}

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

        // ✅ 즐겨찾기 "추가된 경우"만 이벤트
        try {
            boolean nowFav = favoriteService.isFavorite(uid, id);
            if (nowFav) {
                var user = userRepository.getReferenceById(uid);
                var cafe = cafeRepository.getReferenceById(id);
                var ev = userEventRepository.save(UserEvent.favorite(user, cafe));
                segmentRealtimeService.apply(ev);
            }
        } catch (Exception ignore) {}

        return "redirect:/cafes/" + id;
    }

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private static Object invoke(Object bean, String getter) throws Exception {
        return bean.getClass().getMethod(getter).invoke(bean);
    }
    private static Object invokeStrOr(Object bean, String getter, DateTimeFormatter fmt) throws Exception {
        Object v = bean.getClass().getMethod(getter).invoke(bean);
        if (v == null) return null;
        if (v instanceof LocalDateTime) return ((LocalDateTime) v).format(fmt);
        return v;
    }
}

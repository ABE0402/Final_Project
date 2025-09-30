package com.example.hong.controller;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Tag;
import com.example.hong.entity.UserEvent;
import com.example.hong.repository.*;
import com.example.hong.service.FavoriteService;
import com.example.hong.service.ReviewService;
import com.example.hong.service.SegmentRealtimeService;
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

    private final UserRepository userRepository;
    private final UserEventRepository userEventRepository;
    private final SegmentRealtimeService segmentRealtimeService;


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

        //  클릭 이벤트 후 스택 쌓이게 (로그인 사용자만)
        Long uid = meId(auth);
        if (uid != null) {
            try {
                var user = userRepository.getReferenceById(uid);
                var ev = userEventRepository.save(UserEvent.click(user, c));
                segmentRealtimeService.apply(ev); // 즉시 반영
            } catch (Exception ignore) {}
        }

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

            // 리뷰 할때마다 스택 쌓이게
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
        // 즐겨찾기 클릭시 스택 쌓이게
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

    /* ===== helpers ===== */
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

// src/main/java/com/example/HONGAROUND/service/ReviewService.java
package com.example.hong.service;

import com.example.hong.dto.ReviewItemDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Review;
import com.example.hong.entity.User;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.ReviewRepository;
import com.example.hong.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final CafeRepository cafeRepo;
    private final UserRepository userRepo;

    @Value("${app.upload.dir:./uploads}")
    private String uploadRoot;

    private static final String REVIEW_DIR = "reviews";
    private static final String URL_PREFIX = "/uploads/reviews";
    private static final Set<String> IMG_EXT = Set.of("jpg","jpeg","png","gif","webp","bmp");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /* ================== 생성 ================== */
    @Transactional
    public void addWithImages(Long userId, Long cafeId, int rating, String content, List<MultipartFile> images) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("평점은 1~5 사이여야 합니다.");

        User user = userRepo.findById(userId).orElseThrow();
        Cafe cafe = cafeRepo.findById(cafeId).orElseThrow();

        if (cafe.getOwner() != null && cafe.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("본인 매장에는 리뷰를 작성할 수 없습니다.");
        }
        if (reviewRepo.existsByUserAndCafeAndDeletedFalse(user, cafe)) {
            throw new IllegalArgumentException("이미 이 매장에 리뷰를 작성했습니다.");
        }

        List<String> urls = saveUpToFive(images);

        Review.ReviewBuilder b = Review.builder()
                .user(user)
                .cafe(cafe)
                .rating(rating)
                .content(content);

        if (urls.size() > 0) b.imageUrl1(urls.get(0));
        if (urls.size() > 1) b.imageUrl2(urls.get(1));
        if (urls.size() > 2) b.imageUrl3(urls.get(2));
        if (urls.size() > 3) b.imageUrl4(urls.get(3));
        if (urls.size() > 4) b.imageUrl5(urls.get(4));

        reviewRepo.save(b.build());
    }

    /* ================== 조회: 카페 상세용 ================== */
    @Transactional(Transactional.TxType.SUPPORTS) // readOnly 성격
    public List<ReviewItemDto> listForCafeDtos(Long cafeId) {
        return reviewRepo.findForCafeWithUser(cafeId).stream()
                .map(this::toItemDto)
                .toList();
    }

    // (기존 시그니처를 이미 컨트롤러에서 쓰고 있으면 유지하고 내부에서 DTO로 대체)
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ReviewItemDto> listForCafe(Long cafeId) {
        return listForCafeDtos(cafeId);
    }

    private ReviewItemDto toItemDto(Review r) {
        String created = (r.getCreatedAt() == null) ? "" : r.getCreatedAt().format(DT_FMT);
        String nickname = resolveNickname(r.getUser()); // fetch join으로 세션 밖에서도 안전
        return ReviewItemDto.builder()
                .id(r.getId())
                .rating(r.getRating())
                .content(r.getContent())
                .createdAt(created)
                .nickname(nickname)
                .imageUrl1(r.getImageUrl1())
                .imageUrl2(r.getImageUrl2())
                .imageUrl3(r.getImageUrl3())
                .imageUrl4(r.getImageUrl4())
                .imageUrl5(r.getImageUrl5())
                .build();
    }

    /* ================== 조회: 마이페이지용 ================== */
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ReviewItemDto> myReviews(Long userId) {
        return reviewRepo.findByUserIdWithUser(userId).stream()
                .map(this::toItemDto)
                .toList();
    }

    /* ================== 수정/삭제 ================== */
    @Transactional
    public void updateMyReview(Long userId, Long reviewId, String content, Integer rating) {
        var r = reviewRepo.findByIdAndUserIdAndDeletedFalse(reviewId, userId)
                .orElseThrow(() -> new IllegalArgumentException("수정 권한이 없습니다."));
        if (rating != null) r.setRating(rating);
        if (content != null) r.setContent(content);
    }

    @Transactional
    public void deleteMyReview(Long userId, Long reviewId) {
        var r = reviewRepo.findByIdAndUserIdAndDeletedFalse(reviewId, userId)
                .orElseThrow(() -> new IllegalArgumentException("삭제 권한이 없습니다."));
        r.setDeleted(true);
    }

    /* ================== 내부 유틸 ================== */
    private List<String> saveUpToFive(List<MultipartFile> images) {
        List<String> urls = new ArrayList<>();
        if (images == null) return urls;

        try {
            Path dir = Paths.get(uploadRoot, REVIEW_DIR);
            Files.createDirectories(dir);

            int count = 0;
            for (MultipartFile f : images) {
                if (f == null || f.isEmpty()) continue;
                if (++count > 5) break;

                String orig = f.getOriginalFilename();
                String ext = (orig != null && orig.contains(".")) ?
                        orig.substring(orig.lastIndexOf('.') + 1).toLowerCase() : "";
                if (!IMG_EXT.contains(ext)) throw new IllegalArgumentException("이미지 형식이 아닙니다: " + ext);

                String filename = UUID.randomUUID() + "." + ext;
                Files.copy(f.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                urls.add(URL_PREFIX + "/" + filename);
            }
            return urls;
        } catch (Exception e) {
            throw new IllegalStateException("리뷰 이미지를 저장하지 못했습니다.", e);
        }
    }

    private String resolveNickname(User u) {
        if (u == null) return "알 수 없음";
        if (u.getNickname() != null && !u.getNickname().isBlank()) return u.getNickname();
        if (u.getName() != null && !u.getName().isBlank()) return u.getName();
        String email = u.getEmail();
        return (email != null && !email.isBlank()) ? email.split("@")[0] : "사용자";
    }
}

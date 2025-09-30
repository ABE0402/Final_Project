package com.example.hong.service;

import com.example.hong.dto.OwnerReviewDto;
import com.example.hong.entity.*;
import com.example.hong.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerReviewService {

    private final ReviewRepository reviewRepository;
    private final OwnerReplyRepository ownerReplyRepository;
    private final CafeRepository cafeRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    // 내 매장의 리뷰에 대한 답글 DTO
    public List<OwnerReviewDto> listForOwner(Long ownerId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Cafe> cafes = cafeRepository.findByOwner_Id(ownerId);
        List<Restaurant> rests = restaurantRepository.findByOwner_Id(ownerId);

        List<Review> all = new ArrayList<>();
        if (!cafes.isEmpty()) {
            List<Long> cafeIds = cafes.stream().map(Cafe::getId).collect(Collectors.toList());
            all.addAll(reviewRepository.findByCafe_IdInAndDeletedFalseOrderByCreatedAtDesc(cafeIds));
        }
        if (!rests.isEmpty()) {
            List<Long> restIds = rests.stream().map(Restaurant::getId).collect(Collectors.toList());
            all.addAll(reviewRepository.findByRestaurant_IdInAndDeletedFalseOrderByCreatedAtDesc(restIds));
        }
        if (all.isEmpty()) return Collections.emptyList();


        Map<Long, OwnerReply> replyMap = ownerReplyRepository
                .findByReview_IdIn(all.stream().map(Review::getId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(
                        or -> or.getReview().getId(),
                        or -> or,
                        (a, b) -> {
                            LocalDateTime ta = (a.getUpdatedAt() != null) ? a.getUpdatedAt() : a.getCreatedAt();
                            LocalDateTime tb = (b.getUpdatedAt() != null) ? b.getUpdatedAt() : b.getCreatedAt();
                            if (ta == null) return b;
                            if (tb == null) return a;
                            return ta.isAfter(tb) ? a : b;
                        }
                ));

        Map<Long, String> cafeName = cafes.stream().collect(Collectors.toMap(Cafe::getId, Cafe::getName));
        Map<Long, String> restName = rests.stream().collect(Collectors.toMap(Restaurant::getId, Restaurant::getName));

        // DTO 변환
        return all.stream().map(rv -> {
            boolean isCafe = rv.getCafe() != null;
            String targetType = isCafe ? "CAFE" : "RESTAURANT";
            String targetName = isCafe
                    ? cafeName.getOrDefault(rv.getCafe().getId(), "카페")
                    : restName.getOrDefault(rv.getRestaurant().getId(), "레스토랑");

            OwnerReply rep = replyMap.get(rv.getId());

            String reviewCreatedAt = formatTs(rv.getCreatedAt(), rv.getUpdatedAt(), fmt);
            String replyUpdatedAt = (rep != null) ? formatTs(rep.getUpdatedAt(), rep.getCreatedAt(), fmt) : null;

            return OwnerReviewDto.builder()
                    .reviewId(rv.getId())
                    .targetType(targetType)
                    .targetName(targetName)
                    .rating(rv.getRating())
                    .content(rv.getContent())
                    .createdAt(reviewCreatedAt)
                    .authorName(safeName(rv.getUser()))
                    .replyId(rep != null ? rep.getId() : null)
                    .replyContent(rep != null ? rep.getContent() : null)
                    .replyUpdatedAt(replyUpdatedAt)
                    .hasReply(rep != null)
                    .build();
        }).collect(Collectors.toList());
    }

    private String safeName(User u) {
        if (u == null) return "손님";
        if (u.getNickname() != null && !u.getNickname().isBlank()) return u.getNickname();
        if (u.getEmail() != null) return u.getEmail();
        return "사용자";
    }

    private static String formatTs(LocalDateTime primary, LocalDateTime fallback, DateTimeFormatter fmt) {
        LocalDateTime ts = (primary != null) ? primary : fallback;
        return (ts != null) ? ts.format(fmt) : null;
    }

    // 점주 소유권 검사
    private boolean canManage(Long ownerId, Review rv) {
        if (rv.getCafe() != null)       return cafeRepository.existsByIdAndOwner_Id(rv.getCafe().getId(), ownerId);
        if (rv.getRestaurant() != null) return restaurantRepository.existsByIdAndOwner_Id(rv.getRestaurant().getId(), ownerId);
        return false;
    }

    //답급 업서트
    @Transactional
    public void upsertReply(Long ownerId, Long reviewId, String content) {
        if (content == null || content.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "내용을 입력해 주세요.");

        Review rv = reviewRepository.findByIdAndDeletedFalse(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰가 없습니다."));
        if (!canManage(ownerId, rv))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");

        OwnerReply reply = ownerReplyRepository.findByReview_Id(reviewId).orElse(null);
        if (reply == null) {
            User owner = userRepository.findById(ownerId).orElseThrow();
            reply = OwnerReply.builder()
                    .review(rv)
                    .owner(owner)
                    .content(content.trim())
                    .build();
        } else {
            reply.setContent(content.trim());
        }
        ownerReplyRepository.save(reply); // @PrePersist/@PreUpdate로 createdAt/updatedAt 관리
    }

    //댓글 삭제
    @Transactional
    public void deleteReply(Long ownerId, Long reviewId) {
        Review rv = reviewRepository.findByIdAndDeletedFalse(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰가 없습니다."));
        if (!canManage(ownerId, rv))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        ownerReplyRepository.findByReview_Id(reviewId).ifPresent(ownerReplyRepository::delete);
    }
}

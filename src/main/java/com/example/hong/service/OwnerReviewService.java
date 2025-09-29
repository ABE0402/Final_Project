package com.example.hong.service;

import com.example.hong.dto.OwnerReviewDto;
import com.example.hong.entity.*;
import com.example.hong.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    /** 점주: 내 매장(카페+레스토랑) 리뷰 + 답글 DTO */
    public List<OwnerReviewDto> listForOwner(Long ownerId) {
        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 내 매장
        var cafes = cafeRepository.findByOwner_Id(ownerId);
        var rests = restaurantRepository.findByOwner_Id(ownerId);

        List<Review> all = new ArrayList<>();
        if (!cafes.isEmpty()) {
            var cafeIds = cafes.stream().map(Cafe::getId).toList();
            all.addAll(reviewRepository.findByCafe_IdInAndDeletedFalseOrderByCreatedAtDesc(cafeIds));
        }
        if (!rests.isEmpty()) {
            var restIds = rests.stream().map(Restaurant::getId).toList();
            all.addAll(reviewRepository.findByRestaurant_IdInAndDeletedFalseOrderByCreatedAtDesc(restIds));
        }
        if (all.isEmpty()) return List.of();

        // 답글 일괄 로딩
        var replyMap = ownerReplyRepository.findByReview_IdIn(all.stream().map(Review::getId).toList())
                .stream().collect(Collectors.toMap(or -> or.getReview().getId(), or -> or));

        // 이름 맵
        Map<Long,String> cafeName = cafes.stream().collect(Collectors.toMap(Cafe::getId, Cafe::getName));
        Map<Long,String> restName = rests.stream().collect(Collectors.toMap(Restaurant::getId, Restaurant::getName));

        // DTO 변환 (타깃별 분기)
        return all.stream().map(rv -> {
            boolean isCafe = rv.getCafe()!=null;
            String targetType = isCafe ? "CAFE" : "RESTAURANT";
            String targetName = isCafe
                    ? cafeName.getOrDefault(rv.getCafe().getId(), "카페")
                    : restName.getOrDefault(rv.getRestaurant().getId(), "레스토랑");

            var rep = replyMap.get(rv.getId());
            return OwnerReviewDto.builder()
                    .reviewId(rv.getId())
                    .targetType(targetType)
                    .targetName(targetName)
                    .rating(rv.getRating())
                    .content(rv.getContent())
                    .createdAt(rv.getCreatedAt().format(fmt))
                    .authorName(safeName(rv.getUser()))
                    .replyId(rep!=null ? rep.getId() : null)
                    .replyContent(rep!=null ? rep.getContent() : null)
                    .replyUpdatedAt(rep!=null ? rep.getUpdatedAt().format(fmt) : null)
                    .hasReply(rep!=null)
                    .build();
        }).toList();
    }

    private String safeName(User u) {
        if (u == null) return "손님";
        if (u.getNickname()!=null && !u.getNickname().isBlank()) return u.getNickname();
        if (u.getEmail()!=null) return u.getEmail();
        return "사용자";
    }

    /** 점주 소유권 검사 (카페/레스토랑 모두) */
    private boolean canManage(Long ownerId, Review rv) {
        if (rv.getCafe()!=null)       return cafeRepository.existsByIdAndOwner_Id(rv.getCafe().getId(), ownerId);
        if (rv.getRestaurant()!=null) return restaurantRepository.existsByIdAndOwner_Id(rv.getRestaurant().getId(), ownerId);
        return false;
    }

    /** 답글 업서트 */
    @Transactional
    public void upsertReply(Long ownerId, Long reviewId, String content) {
        if (content==null || content.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "내용을 입력해 주세요.");

        Review rv = reviewRepository.findByIdAndDeletedFalse(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰가 없습니다."));
        if (!canManage(ownerId, rv))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");

        var reply = ownerReplyRepository.findByReview_Id(reviewId).orElse(null);
        if (reply == null) {
            var owner = userRepository.findById(ownerId).orElseThrow();
            reply = OwnerReply.builder()
                    .review(rv)
                    .owner(owner)
                    .content(content.trim())
                    .build();
        } else {
            reply.setContent(content.trim());
        }
        ownerReplyRepository.save(reply);
    }

    /** 답글 삭제 */
    @Transactional
    public void deleteReply(Long ownerId, Long reviewId) {
        Review rv = reviewRepository.findByIdAndDeletedFalse(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰가 없습니다."));
        if (!canManage(ownerId, rv))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        ownerReplyRepository.findByReview_Id(reviewId).ifPresent(ownerReplyRepository::delete);
    }
}

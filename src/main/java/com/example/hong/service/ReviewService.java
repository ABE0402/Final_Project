package com.example.hong.service;

import com.example.hong.dto.ReviewDto;
import com.example.hong.service.TagService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final List<ReviewDto> reviewList = new ArrayList<>();
    private long nextReviewId = 1;

    // TagService를 주입받아 모든 게시물 ID를 가져옵니다.
    private final TagService tagService;

    public ReviewService(TagService tagService) {
        this.tagService = tagService;
        // 서비스 초기화 시 더미 리뷰 데이터 생성
        createDummyReviews();
    }

    private void createDummyReviews() {
        List<Long> allPostIds = tagService.getAllPostIds();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        // 더미 이미지 경로 리스트
        List<String> dummyImages = List.of(
                "/images/review1.jpg",
                "/images/review2.jpg",
                "/images/review3.jpg",
                "/images/review4.jpg"
        );

        for (Long postId : allPostIds) {
            for (int i = 1; i <= 10; i++) {
                String reviewDate = LocalDateTime.now().minusDays(10 - i).format(formatter);

                // 3번째 리뷰마다 이미지를 추가
                String reviewImage = (i % 3 == 0) ? dummyImages.get((int) (Math.random() * dummyImages.size())) : null;

                reviewList.add(new ReviewDto(
                        nextReviewId++,
                        postId,
                        "사용자" + i,
                        "게시물 " + postId + "에 대한 리뷰 " + i + "입니다.",
                        reviewDate,
                        reviewImage
                ));
            }
        }
    }

    // 특정 장소에 대한 리뷰 목록 조회
    public List<ReviewDto> getReviewsByPlaceId(Long placeId) {
        return reviewList.stream()
                .filter(review -> review.getPlaceId().equals(placeId))
                .collect(Collectors.toList());
    }

    // 새로운 리뷰 저장
    public void saveReview(Long placeId, ReviewDto reviewDto) {
        reviewDto.setId(nextReviewId++);
        reviewDto.setPlaceId(placeId);
        reviewList.add(reviewDto);
    }
}
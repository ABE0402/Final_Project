package com.example.hong.repository;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.entity.Cafe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

// JpaRepository와 우리가 만든 CafeRepositoryCustom을 함께 상속받습니다.
public interface CafeRepository extends JpaRepository<Cafe, Long>, CafeRepositoryCustom {

    // Fetch Join을 사용하여 Cafe를 조회할 때 연관된 CafeTag와 Tag 정보까지 한 번에 가져오는 쿼리
    @Query("""
        SELECT DISTINCT c
        FROM Cafe c
        LEFT JOIN FETCH c.cafeTags ct
        LEFT JOIN FETCH ct.tag
    """)
    List<Cafe> findAllWithTags();

    List<Cafe> findTop8ByApprovalStatusAndIsVisibleOrderByAverageRatingDescReviewCountDesc(
            ApprovalStatus status, boolean isVisible);
    List<Cafe> findByOwner_IdOrderByCreatedAtDesc(Long ownerId);

    // 오너가 소유한 특정 카페 단건 접근(권한 체크에 유용)
    Optional<Cafe> findByIdAndOwnerId(Long cafeId, Long ownerId);

    long countByOwnerId(Long ownerId);


    // 관리자: 대기 목록
    List<Cafe> findByApprovalStatusOrderByCreatedAtAsc(ApprovalStatus status);

    // 관리자: 승인된 가게 목록(최근 수정 순)
    List<Cafe> findByApprovalStatusOrderByUpdatedAtDesc(ApprovalStatus status);

    // 관리자: 승인 + 가시성 필터
    List<Cafe> findByApprovalStatusAndIsVisibleOrderByUpdatedAtDesc(ApprovalStatus status, boolean isVisible);

    Page<Cafe> findByApprovalStatusAndIsVisible(ApprovalStatus status, boolean isVisible, Pageable pageable);

    Page<Cafe> findByApprovalStatusAndIsVisibleAndCafeTags_Tag_Id(
            ApprovalStatus status, boolean isVisible, Integer tagId, Pageable pageable
    );

    // ✅ 태그로 필터 + "추천 정렬(평점 desc, 리뷰수 desc)" Top N (빠른 호출용)
    List<Cafe> findTop8ByApprovalStatusAndIsVisibleAndCafeTags_Tag_IdOrderByAverageRatingDescReviewCountDesc(
            ApprovalStatus status, boolean isVisible, Integer tagId
    );
}
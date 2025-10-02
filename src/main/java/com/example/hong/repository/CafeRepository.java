package com.example.hong.repository;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.entity.Cafe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    @Query(
            value = """
                        select c
                        from Cafe c
                        join c.cafeTags ct
                        where c.approvalStatus = :status
                          and c.isVisible = :visible
                          and ct.tag.id = :tagId
                    """,
            countQuery = """
                        select count(c)
                        from Cafe c
                        join c.cafeTags ct
                        where c.approvalStatus = :status
                          and c.isVisible = :visible
                          and ct.tag.id = :tagId
                    """
    )
    Page<Cafe> findByTagPaged(@Param("status") ApprovalStatus status,
                              @Param("visible") boolean visible,
                              @Param("tagId") Integer tagId,
                              Pageable pageable);

    /* ===== 카드에 #태그 표시용 벌크 조회 (프로젝션) =====
       여러 카페의 태그명을 한 번에 가져옵니다. N+1 방지용.
       - 반환은 (cafeId, tagName) 튜플 리스트
     */
    interface CafeIdTagName {
        Long getCafeId();

        String getName();
    }

    @Query("""
                select ct.cafe.id as cafeId, t.name as name
                from CafeTag ct
                join ct.tag t
                where ct.cafe.id in :cafeIds
            """)
    List<CafeIdTagName> findTagNamesByCafeIdIn(@Param("cafeIds") Collection<Long> cafeIds);


    List<Cafe> findByOwner_Id(Long ownerId);
    boolean existsByIdAndOwner_Id(Long id, Long ownerId);

//    /* 오수현씨의 검색 기능 */
//    @Query("""
//        SELECT DISTINCT c
//        FROM Cafe c
//        LEFT JOIN c.menus m
//        WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
//           OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
//           OR LOWER(c.addressRoad) LIKE LOWER(CONCAT('%', :keyword, '%'))
//    """)
//    List<Cafe> searchByKeyword(@Param("keyword") String keyword);
    //LEFT JOIN → 메뉴까지 검색 가능
    //DISTINCT → 하나의 카페가 여러 메뉴에 걸쳐 중복 출력되는 것 방지
    List<Cafe> findByIdInAndApprovalStatusAndIsVisible(List<Long> ids, ApprovalStatus approvalStatus, boolean isVisible);

    @Query("""
        select distinct c
        from Cafe c
        left join fetch c.cafeTags ct
        left join fetch ct.tag
        where c.approvalStatus = :status
    """)
    List<Cafe> findAllWithTagsByStatus(@Param("status") ApprovalStatus status);

    boolean existsByIdAndApprovalStatus(Long id, ApprovalStatus status);

}
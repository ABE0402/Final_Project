package com.example.hong.repository;

import com.example.hong.entity.Cafe;
import com.example.hong.entity.Review;
import com.example.hong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCafeAndDeletedFalseOrderByIdDesc(Cafe cafe);
    boolean existsByUserAndCafeAndDeletedFalse(User user, Cafe cafe);

    long countByCafeIdAndDeletedFalse(Long cafeId);
    // 평균은 서비스에서 스트림/JPQL로 계산해도 OK (여기선 서비스에서 계산)

    // ⬇️ 마이페이지용 (내 리뷰 목록/수정/삭제)
    List<Review> findByUserIdAndDeletedFalseOrderByIdDesc(Long userId);
    Optional<Review> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    @Query("""
    select r from Review r
    join fetch r.user u
    left join fetch r.reviewAspectScores s
    where r.cafe.id = :cafeId and r.deleted = false
    order by r.id desc
    """)
    List<Review> findForCafeWithUser(@Param("cafeId") Long cafeId);

    @Query("""
        select r from Review r
        join fetch r.user u
        left join fetch r.reviewAspectScores s
        where r.user.id = :userId and r.deleted = false
        order by r.id desc
    """)
    List<Review> findByUserIdWithUser(@Param("userId") Long userId);

    //카페 - 레스토랑 리뷰 구분

    // 카페 리뷰(여러 매장 id)
    List<Review> findByCafe_IdInAndDeletedFalseOrderByCreatedAtDesc(Collection<Long> cafeIds);

    // 레스토랑 리뷰(여러 매장 id) — 레스토랑 리뷰가 있다면 사용
    List<Review> findByRestaurant_IdInAndDeletedFalseOrderByCreatedAtDesc(Collection<Long> restaurantIds);

    Optional<Review> findByIdAndDeletedFalse(Long id);

    @Query("""
           select r.cafe.id as cafeId, count(r.id) as cnt
             from Review r
            where r.deleted = false
              and r.cafe.id in :cafeIds
            group by r.cafe.id
           """)
    List<CafeReviewCount> countByCafeIds(Collection<Long> cafeIds);

    interface CafeReviewCount {
        Long getCafeId();
        Long getCnt();
    }

    @Query("""
    select r from Review r
    left join fetch r.cafe c
    left join fetch r.restaurant rest
    join fetch r.user u
    where (:deleted is null or r.deleted = :deleted)
    and (
       :target is null
       or (:target = 'CAFE' and c.id is not null)
       or (:target = 'RESTAURANT' and rest.id is not null)
       )
    and (
       :q = '' 
       or lower(coalesce(r.content,'')) like lower(concat('%', :q, '%'))
       or lower(coalesce(u.nickname,'')) like lower(concat('%', :q, '%'))
       or lower(coalesce(u.email,'')) like lower(concat('%', :q, '%'))
       or lower(coalesce(c.name,'')) like lower(concat('%', :q, '%'))
       or lower(coalesce(rest.name,'')) like lower(concat('%', :q, '%'))
    )
    order by r.createdAt desc
    """)
    List<Review> adminSearch(@Param("q") String q,
                             @Param("target") String target,   // null/CAFE/RESTAURANT
                             @Param("deleted") Boolean deleted); // null/true/false
}

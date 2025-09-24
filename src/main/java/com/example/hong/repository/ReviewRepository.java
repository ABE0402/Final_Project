package com.example.hong.repository;

import com.example.hong.entity.Cafe;
import com.example.hong.entity.Review;
import com.example.hong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}

package com.example.hong.repository;

import com.example.hong.entity.Cafe;
import com.example.hong.entity.Favorite;
import com.example.hong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserAndCafe(User user, Cafe cafe);
    Optional<Favorite> findByUserAndCafe(User user, Cafe cafe);
    long countByCafeId(Long cafeId);
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
  select f from Favorite f
  join fetch f.cafe c
  where f.user.id = :uid
  order by f.id desc
""")
    List<Favorite> findWithCafeByUserId(@Param("uid") Long uid);
}

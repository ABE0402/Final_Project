package com.example.hong.repository;

import com.example.hong.domain.ApprovalStatus;
import com.example.hong.entity.Restaurant;
import com.example.hong.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findTop8ByApprovalStatusAndIsVisibleOrderByAverageRatingDescReviewCountDesc(
            ApprovalStatus status, boolean isVisible);

    Page<Restaurant> findByApprovalStatusAndIsVisible(ApprovalStatus status, boolean isVisible, Pageable pageable);

    List<Restaurant> findByOwner_Id(Long ownerId);
    boolean existsByIdAndOwner_Id(Long id, Long ownerId);

}

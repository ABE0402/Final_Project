package com.example.hong.repository;

import com.example.hong.entity.OwnerReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OwnerReplyRepository extends JpaRepository<OwnerReply, Long> {
    Optional<OwnerReply> findByReview_Id(Long reviewId);
    List<OwnerReply> findByReview_IdIn(Collection<Long> reviewIds);
    boolean existsByReview_Id(Long reviewId);
}
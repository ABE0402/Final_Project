package com.example.hong.repository;

import com.example.hong.domain.ReservationStatus;
import com.example.hong.domain.ReserveTargetType;
import com.example.hong.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByUser_IdAndTargetTypeAndTargetIdAndReservationAt(
            Long userId, ReserveTargetType targetType, Long targetId, LocalDateTime at);

    List<Reservation> findByUser_IdOrderByReservationAtDesc(Long userId);

    List<Reservation> findByUser_IdAndTargetTypeOrderByReservationAtDesc(Long userId, ReserveTargetType type);

    long countByTargetTypeAndTargetIdAndReservationAt(ReserveTargetType type, Long targetId, LocalDateTime at);
    List<Reservation> findByTargetTypeAndTargetIdInOrderByReservationAtAsc(
            ReserveTargetType type, Collection<Long> targetIds);

    List<Reservation> findByTargetTypeAndTargetIdInAndStatusOrderByReservationAtAsc(
            ReserveTargetType type, Collection<Long> targetIds, ReservationStatus status);

    List<Reservation> findByTargetTypeAndTargetIdInOrderByReservationAtDesc(ReserveTargetType type, Collection<Long> targetIds);
}

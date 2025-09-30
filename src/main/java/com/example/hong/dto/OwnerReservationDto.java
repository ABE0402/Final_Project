package com.example.hong.dto;

import com.example.hong.domain.ReservationStatus;
import com.example.hong.domain.ReserveTargetType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OwnerReservationDto {
    private Long id;
    private ReserveTargetType targetType; // CAFE / RESTAURANT
    private String targetName;            // 매장명
    private String reservationAt;         // yyyy-MM-dd HH:mm (뷰용 포맷 문자열)
    private int partySize;
    private ReservationStatus status;

    // 머스태치에서 바로 쓰게 boolean도 제공
    public boolean getIsPending()   { return status == ReservationStatus.PENDING; }
    public boolean getIsConfirmed() { return status == ReservationStatus.CONFIRMED; }
    public boolean getIsCancelled() { return status == ReservationStatus.CANCELLED; }
}
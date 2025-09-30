package com.example.hong.service;

import com.example.hong.domain.ReservationStatus;
import com.example.hong.domain.ReserveTargetType;
import com.example.hong.dto.OwnerReservationDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Reservation;
import com.example.hong.entity.Restaurant;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.ReservationRepository;
import com.example.hong.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {
    private final ReservationRepository reservationRepository;
    private final CafeRepository cafeRepository;
    private final RestaurantRepository restaurantRepository;
    private final NotificationService notificationService;

    //예약 DTO로 변환
    public List<OwnerReservationDto> listReservations(Long ownerId) {
        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Cafe> cafes = cafeRepository.findByOwner_Id(ownerId);
        List<Restaurant> rests = restaurantRepository.findByOwner_Id(ownerId);

        Map<Long,String> cafeName = cafes.stream().collect(Collectors.toMap(Cafe::getId, Cafe::getName));
        Map<Long,String> restName = rests.stream().collect(Collectors.toMap(Restaurant::getId, Restaurant::getName));

        List<OwnerReservationDto> out = new ArrayList<>();

        if (!cafes.isEmpty()) {
            var ids = cafes.stream().map(Cafe::getId).toList();
            var list = reservationRepository
                    .findByTargetTypeAndTargetIdInOrderByReservationAtDesc(ReserveTargetType.CAFE, ids)
                    .stream()
                    .map(r -> OwnerReservationDto.builder()
                            .id(r.getId())
                            .targetType(ReserveTargetType.CAFE)
                            .targetName(cafeName.getOrDefault(r.getTargetId(), "카페"))
                            .reservationAt(r.getReservationAt().format(fmt))
                            .partySize(r.getPartySize())
                            .status(r.getStatus())
                            .build())
                    .toList();
            out.addAll(list);
        }
        if (!rests.isEmpty()) {
            var ids = rests.stream().map(Restaurant::getId).toList();
            var list = reservationRepository
                    .findByTargetTypeAndTargetIdInOrderByReservationAtDesc(ReserveTargetType.RESTAURANT, ids)
                    .stream()
                    .map(r -> OwnerReservationDto.builder()
                            .id(r.getId())
                            .targetType(ReserveTargetType.RESTAURANT)
                            .targetName(restName.getOrDefault(r.getTargetId(), "레스토랑"))
                            .reservationAt(r.getReservationAt().format(fmt))
                            .partySize(r.getPartySize())
                            .status(r.getStatus())
                            .build())
                    .toList();
            out.addAll(list);
        }

        out.sort(Comparator.comparing(OwnerReservationDto::getReservationAt).reversed());
        return out;
    }

    private boolean isOwnerOfTarget(Long ownerId, Reservation r){
        return (r.getTargetType()==ReserveTargetType.CAFE)
                ? cafeRepository.existsByIdAndOwner_Id(r.getTargetId(), ownerId)
                : restaurantRepository.existsByIdAndOwner_Id(r.getTargetId(), ownerId);
    }

    @Transactional
    public void confirm(Long actorOwnerId, Long reservationId, boolean isAdmin) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약이 없습니다."));
        if (!isAdmin && !isOwnerOfTarget(actorOwnerId, r))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        if (r.getStatus() == ReservationStatus.CONFIRMED) return;

        r.setStatus(ReservationStatus.CONFIRMED);

        String targetName = (r.getTargetType()==ReserveTargetType.CAFE)
                ? cafeRepository.findById(r.getTargetId()).map(Cafe::getName).orElse("카페")
                : restaurantRepository.findById(r.getTargetId()).map(Restaurant::getName).orElse("레스토랑");

        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String when = r.getReservationAt().format(fmt);

        // 사용자 알림
        notificationService.push(
                r.getUser().getId(),
                "‘" + targetName + "’ 예약이 승인되었습니다. (" + when + ")",
                "/mypage/reservations"
        );
    }

    @Transactional
    public void ownerCancel(Long actorOwnerId, Long reservationId, boolean isAdmin) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약이 없습니다."));
        if (!isAdmin && !isOwnerOfTarget(actorOwnerId, r))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        if (r.getStatus() == ReservationStatus.CANCELLED) return;

        r.setStatus(ReservationStatus.CANCELLED);

        String targetName = (r.getTargetType()==ReserveTargetType.CAFE)
                ? cafeRepository.findById(r.getTargetId()).map(Cafe::getName).orElse("카페")
                : restaurantRepository.findById(r.getTargetId()).map(Restaurant::getName).orElse("레스토랑");

        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String when = r.getReservationAt().format(fmt);

        //사용자 알림
        notificationService.push(
                r.getUser().getId(),
                "‘" + targetName + "’ 예약이 취소되었습니다. (" + when + ")",
                "/mypage/reservations"
        );
    }
}

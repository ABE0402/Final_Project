package com.example.hong.service;

import com.example.hong.domain.ReservationStatus;
import com.example.hong.domain.ReserveTargetType;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Reservation;
import com.example.hong.entity.User;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.ReservationRepository;
import com.example.hong.repository.UserRepository;
import com.example.hong.repository.RestaurantRepository; // 존재한다고 가정
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final CafeRepository cafeRepository;
    private final RestaurantRepository restaurantRepository;

    private final NotificationService notificationService;

    @Transactional
    public Reservation create(Long userId,
                              ReserveTargetType targetType,
                              Long targetId,
                              LocalDateTime at,
                              int partySize) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "사용자를 찾을 수 없습니다."));


        String targetName = switch (targetType) {
            case CAFE -> cafeRepository.findById(targetId)
                    .map(Cafe::getName).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "카페가 없습니다."));
            case RESTAURANT -> restaurantRepository.findById(targetId)
                    .map(r -> r.getName()).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "레스토랑이 없습니다."));
        };

        if (reservationRepository.existsByUser_IdAndTargetTypeAndTargetIdAndReservationAt(userId, targetType, targetId, at)) {
            throw new ResponseStatusException(CONFLICT, "이미 동일한 시간에 예약이 존재합니다.");
        }

        Reservation saved = reservationRepository.save(
                Reservation.builder()
                        .user(user)
                        .targetType(targetType)
                        .targetId(targetId)
                        .reservationAt(at)
                        .partySize(partySize)
                        .status(ReservationStatus.PENDING)
                        .build()
        );

        var fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String when = saved.getReservationAt().format(fmt);
        notificationService.push(
                user.getId(),
                "‘" + targetName + "’ 에 예약되었습니다. (" + when + ")",
                "/mypage/reservations"
        );


        Long ownerId = (targetType == ReserveTargetType.CAFE)
                ? cafeRepository.findById(targetId).map(c -> c.getOwner().getId()).orElse(null)
                : restaurantRepository.findById(targetId).map(r -> r.getOwner().getId()).orElse(null);
        if (ownerId != null) {
            notificationService.push(ownerId,
                    "새 예약 도착: ‘" + targetName + "’ (" + when + ", " + partySize + "명)",
                    "/owner/reservations");
        }

        return saved;
    }

    public List<Reservation> myAll(Long userId) {
        return reservationRepository.findByUser_IdOrderByReservationAtDesc(userId);
    }

    public List<Reservation> myByType(Long userId, ReserveTargetType type) {
        return reservationRepository.findByUser_IdAndTargetTypeOrderByReservationAtDesc(userId, type);
    }

    @Transactional
    public void cancel(Long userId, Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "예약이 존재하지 않습니다."));
        if (!res.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "본인 예약만 취소할 수 있습니다.");
        }
        if (res.getStatus() == ReservationStatus.CANCELLED) return;
        res.setStatus(ReservationStatus.CANCELLED);

        String targetName = "(예약 대상)";
        if (res.getTargetType() == ReserveTargetType.CAFE) {
            targetName = cafeRepository.findById(res.getTargetId()).map(Cafe::getName).orElse(targetName);
        } else {
            targetName = restaurantRepository.findById(res.getTargetId()).map(r -> r.getName()).orElse(targetName);
        }

        var fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String when = res.getReservationAt().format(fmt);

        // ✅ 사용자 알림(문구 통일)
        notificationService.push(
                userId,
                "‘" + targetName + "’ 예약이 취소되었습니다. (" + when + ")",
                "/mypage/reservations"
        );

        // (옵션) 점주 알림도 원하면 주석 해제
        Long ownerId = (res.getTargetType() == ReserveTargetType.CAFE)
                ? cafeRepository.findById(res.getTargetId()).map(c -> c.getOwner().getId()).orElse(null)
                : restaurantRepository.findById(res.getTargetId()).map(r -> r.getOwner().getId()).orElse(null);
        if (ownerId != null) {
            notificationService.push(
                    ownerId,
                    "고객 취소: ‘" + targetName + "’ (" + when + ", " + res.getPartySize() + "명)",
                    "/owner/reservations"
            );
        }
    }
}

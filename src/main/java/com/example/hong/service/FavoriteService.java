package com.example.hong.service;

import com.example.hong.dto.FavoriteItemDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Favorite;
import com.example.hong.entity.User;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.FavoriteRepository;
import com.example.hong.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final CafeRepository cafeRepository;
    private final UserRepository userRepository;

    public boolean isFavorite(Long userId, Long cafeId) {
        User u = userRepository.findById(userId).orElseThrow();
        Cafe c = cafeRepository.findById(cafeId).orElseThrow();
        return favoriteRepository.existsByUserAndCafe(u, c);
    }

    @Transactional
    public void toggle(Long userId, Long cafeId) {
        User u = userRepository.findById(userId).orElseThrow();
        Cafe c = cafeRepository.findById(cafeId).orElseThrow();

        var opt = favoriteRepository.findByUserAndCafe(u, c);
        if (opt.isPresent()) {
            favoriteRepository.delete(opt.get());
        } else {
            favoriteRepository.save(Favorite.builder().user(u).cafe(c).build());
        }
    }

    public List<Favorite> myFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void remove(Long userId, Long cafeId) {
        User u = userRepository.findById(userId).orElseThrow();
        Cafe c = cafeRepository.findById(cafeId).orElseThrow();
        favoriteRepository.findByUserAndCafe(u, c).ifPresent(favoriteRepository::delete);
    }

    @Transactional
    public List<FavoriteItemDto> myFavoritesDto(Long userId) {
        return favoriteRepository.findWithCafeByUserId(userId).stream()
                .map(f -> FavoriteItemDto.builder()
                        .cafeId(f.getCafe().getId())
                        .name(f.getCafe().getName())
                        .address(f.getCafe().getAddressRoad())
                        .heroImageUrl(f.getCafe().getHeroImageUrl())
                        .averageRating(
                                f.getCafe().getAverageRating() == null
                                        ? 0.0
                                        : f.getCafe().getAverageRating().doubleValue())
                        .reviewCount(f.getCafe().getReviewCount())
                        .build())
                .toList();
    }
}
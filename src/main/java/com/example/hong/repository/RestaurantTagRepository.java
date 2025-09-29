package com.example.hong.repository;

import com.example.hong.entity.Restaurant;
import com.example.hong.entity.RestaurantTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantTagRepository extends JpaRepository<RestaurantTag, RestaurantTag.RestaurantTagId> {
    List<RestaurantTag> findByRestaurant(Restaurant restaurant);
    void deleteByRestaurant(Restaurant restaurant);
}

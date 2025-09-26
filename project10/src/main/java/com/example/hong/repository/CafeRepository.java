package com.example.hong.repository;

import com.example.hong.entity.Cafe;
import com.example.hong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
    @Query("""
        SELECT DISTINCT c
        FROM Cafe c
        LEFT JOIN c.menus m
        WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(c.addressRoad) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<Cafe> searchByKeyword(@Param("keyword") String keyword);
    //LEFT JOIN → 메뉴까지 검색 가능
    //DISTINCT → 하나의 카페가 여러 메뉴에 걸쳐 중복 출력되는 것 방지
}

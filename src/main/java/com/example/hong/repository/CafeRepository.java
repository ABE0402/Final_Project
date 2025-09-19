package com.example.hong.repository;

import com.example.hong.entity.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// JpaRepository와 우리가 만든 CafeRepositoryCustom을 함께 상속받습니다.
public interface CafeRepository extends JpaRepository<Cafe, Long>, CafeRepositoryCustom {

    // Fetch Join을 사용하여 Cafe를 조회할 때 연관된 CafeTag와 Tag 정보까지 한 번에 가져오는 쿼리
    @Query("""
        SELECT DISTINCT c
        FROM Cafe c
        LEFT JOIN FETCH c.cafeTags ct
        LEFT JOIN FETCH ct.tag
    """)
    List<Cafe> findAllWithTags();
}
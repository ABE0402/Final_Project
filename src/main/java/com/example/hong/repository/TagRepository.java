package com.example.hong.repository;

import com.example.hong.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    // 태그 이름(tagName) 목록으로 해당 Tag 엔티티들을 모두 조회하는 메서드
    List<Tag> findByNameIn(List<String> names);
}
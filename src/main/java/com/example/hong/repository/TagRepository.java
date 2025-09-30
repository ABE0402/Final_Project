package com.example.hong.repository;

import com.example.hong.domain.TagAppliesTo;
import com.example.hong.entity.Tag;
import io.micrometer.observation.ObservationFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    // 태그 이름(tagName) 목록으로 해당 Tag 엔티티들을 모두 조회하는 메서드
    List<Tag> findByNameIn(List<String> names);

    // 카테고리 + 이름으로 단건 찾기 (Optional)
    Optional<Tag> findByCategoryAndName(String category, String name);

    // 카테고리별 이름 오름차순 + 페이징(간단히 List로 받기)

    Page<Tag> findByCategoryOrderByNameAsc(String category, Pageable pageable);

    // ✅ 카테고리 전체 목록(페이징 없이)
    List<Tag> findByCategoryOrderByNameAsc(String category);


    // ✅ id 목록으로 조회
    List<Tag> findByIdIn(Collection<Integer> ids);

    List<Tag> findByCategoryAndAppliesToInOrderByDisplayOrderAscNameAsc(
            String category, Collection<TagAppliesTo> scopes
    );

}
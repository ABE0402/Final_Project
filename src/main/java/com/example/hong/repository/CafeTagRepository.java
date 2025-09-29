package com.example.hong.repository;

import com.example.hong.entity.Cafe;
import com.example.hong.entity.CafeTag;
import com.example.hong.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CafeTagRepository extends JpaRepository<CafeTag, CafeTag.CafeTagId> {
    List<CafeTag> findByCafe(Cafe cafe);
    void deleteByCafe(Cafe cafe);

    //존재여부
    boolean existsByCafeAndTag(Cafe cafe, Tag tag);

    // 엔티티 통째로
    List<CafeTag> findByCafe_Id(Long cafeId);

    // 태그 ID만
    @Query("select ct.tag.id from CafeTag ct where ct.cafe.id = :cafeId")
    List<Integer> findTagIdsByCafeId(Long cafeId);

    // 태그 엔티티
    @Query("select t from CafeTag ct join ct.tag t where ct.cafe.id = :cafeId")
    List<Tag> findTagsByCafeId(Long cafeId);

    interface CafeIdTagName {
        Long getCafeId();
        String getName();
    }

    @Query("""
  select ct.cafe.id as cafeId, t.name as name
  from CafeTag ct join ct.tag t
  where ct.cafe.id in :cafeIds
  """)
    List<CafeIdTagName> findTagNamesByCafeIds(@Param("cafeIds") Collection<Long> cafeIds);
}

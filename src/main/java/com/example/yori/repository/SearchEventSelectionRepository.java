package com.example.yori.repository;

import com.example.yori.dto.TagFrequencyDto; // DTO import
import com.example.yori.entity.SearchEventSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
// import java.util.Map; // Map은 더 이상 필요 없으므로 삭제 가능

public interface SearchEventSelectionRepository extends JpaRepository<SearchEventSelection, SearchEventSelection.SearchEventSelectionId> {

    @Query("SELECT new com.example.yori.dto.TagFrequencyDto(s.tag, COUNT(s.tag)) " +
            "FROM SearchEventSelection s " +
            "JOIN s.searchEvent se " +
            "WHERE se.userId = :userId " + // se.user.id -> se.userId 로 수정
            "GROUP BY s.tag " +
            "ORDER BY COUNT(s.tag) DESC")
    List<TagFrequencyDto> findUserTagFrequencies(@Param("userId") Long userId);
}
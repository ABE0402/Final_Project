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

    List<Tag> findByNameIn(List<String> names);

    Optional<Tag> findByCategoryAndName(String category, String name);

    Page<Tag> findByCategoryOrderByNameAsc(String category, Pageable pageable);

    List<Tag> findByCategoryOrderByNameAsc(String category);

    List<Tag> findByIdIn(Collection<Integer> ids);

    List<Tag> findByCategoryAndAppliesToInOrderByDisplayOrderAscNameAsc(
            String category, Collection<TagAppliesTo> scopes
    );

}
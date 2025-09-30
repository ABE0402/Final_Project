package com.example.hong.repository;

import com.example.hong.dto.SearchRequestDto;
import com.example.hong.entity.Cafe;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.hong.entity.QCafe.cafe;

@RequiredArgsConstructor
public class CafeRepositoryCustomImpl implements CafeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Cafe> search(SearchRequestDto condition) {
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(condition.getQuery())) {
            builder.and(keywordContains(condition.getQuery()));
        }

        List<String> allTagNames = extractFilterTags(condition);
        if (allTagNames != null && !allTagNames.isEmpty()) {
            builder.and(hasAllTags(allTagNames));
        }

        return queryFactory
                .selectFrom(cafe)
                .where(builder)
                .orderBy(sort(condition.getSort()))
                .fetch();
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return cafe.name.containsIgnoreCase(keyword)
                .or(cafe.description.containsIgnoreCase(keyword))
                .or(cafe.addressRoad.containsIgnoreCase(keyword));
    }

    private BooleanExpression hasAllTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return null;
        }
        return tagNames.stream()
                .map(this::cafeHasTag)
                .reduce(BooleanExpression::and)
                .orElse(null);
    }

    private BooleanExpression cafeHasTag(String tagName) {
        return cafe.cafeTags.any().tag.name.eq(tagName);
    }

    private OrderSpecifier<?> sort(String sortValue) {
        if (!StringUtils.hasText(sortValue)) {
            return cafe.id.desc(); // 기본 정렬
        }
        if ("rating".equalsIgnoreCase(sortValue)) {
            return cafe.averageRating.desc();
        }
        if ("reviews".equalsIgnoreCase(sortValue)) {
            return cafe.reviewCount.desc();
        }
        if ("like".equalsIgnoreCase(sortValue)) {
            return cafe.favoritesCount.desc();
        }
        return cafe.id.desc();
    }

    private List<String> extractFilterTags(SearchRequestDto request) {
        List<String> tagNames = new ArrayList<>();
        addTagsToList(tagNames, request.getCompanion());
        addTagsToList(tagNames, request.getMood());
        addTagsToList(tagNames, request.getAmenities());
        addTagsToList(tagNames, request.getReservation());
        addTagsToList(tagNames, request.getType());
        return tagNames;
    }

    private void addTagsToList(List<String> list, String commaSeparatedTags) {
        if (StringUtils.hasText(commaSeparatedTags)) {
            list.addAll(Arrays.stream(commaSeparatedTags.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()));
        }
    }
}
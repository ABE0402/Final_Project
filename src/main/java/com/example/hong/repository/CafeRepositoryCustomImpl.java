package com.example.hong.repository;

import com.example.hong.dto.SearchRequestDto;
import com.example.hong.entity.Cafe;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.hong.entity.QCafe.cafe;
import static com.example.hong.entity.QCafeTag.cafeTag;
import static com.example.hong.entity.QTag.tag;

@RequiredArgsConstructor
public class CafeRepositoryCustomImpl implements CafeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Cafe> search(SearchRequestDto condition) {
        return queryFactory
                .select(cafe).from(cafe)
                // 태그(필터) 조건을 위해 JOIN
                .leftJoin(cafe.cafeTags, cafeTag)
                .leftJoin(cafeTag.tag, tag)
                .where(
                        // 각 필터 조건을 AND로 연결 (조건이 없으면 null 반환하여 무시됨)
                        keywordContains(condition.getQuery()),
                        companionIn(condition.getCompanion()),
                        moodIn(condition.getMood()),
                        amenitiesIn(condition.getAmenities())
                )
                .groupBy(cafe.id) // 태그 Join으로 인한 결과 중복을 제거
                .orderBy(sort(condition.getSort())) // 정렬 조건 적용
                .fetch();
    }

    // 1. 텍스트 검색어 (가게 이름, 설명)
    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? cafe.name.contains(keyword).or(cafe.description.contains(keyword)) : null;
    }

    // 2. 동반인 필터
    private BooleanExpression companionIn(String companion) {
        return tagsIn("companion", companion);
    }

    // 3. 분위기 필터
    private BooleanExpression moodIn(String mood) {
        return tagsIn("mood", mood);
    }

    // 4. 편의시설 필터
    private BooleanExpression amenitiesIn(String amenities) {
        return tagsIn("amenities", amenities);
    }

    // (공통 로직) 태그 처리 메서드
    private BooleanExpression tagsIn(String category, String tagValues) {
        if (!StringUtils.hasText(tagValues)) {
            return null;
        }
        List<String> tagList = List.of(tagValues.split(","));
        return tag.category.eq(category).and(tag.name.in(tagList));
    }

    // 5. 정렬 조건
    private OrderSpecifier<?> sort(String sort) {
        if ("rating".equals(sort)) {
            return cafe.averageRating.desc(); // 평점 높은 순
        }
        if ("reviews".equals(sort)) {
            return cafe.reviewCount.desc(); // 리뷰 많은 순
        }
        // 기본 정렬은 최신순 (ID 역순)
        return cafe.id.desc();
    }
}
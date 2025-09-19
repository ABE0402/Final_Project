package com.example.demo.service;

import com.example.demo.dto.PostDto;
import com.example.demo.dto.TagSection;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final Random rnd = new Random();
    private final List<PostDto> allPosts = new ArrayList<>();

    public TagService() {
        // 더미 데이터 초기화
        createDummyData("카페", "cafe", List.of("카페", "디저트", "브런치", "루프탑", "데이트", "혼밥", "술집", "바"));
        createDummyData("음식점", "restaurant", List.of("식당", "한식", "일식", "중식", "분식", "고기집", "족발보쌈", "야시장", "포장마차"));
    }

    private void createDummyData(String titlePrefix, String category, List<String> tags) {
        for (String tag : tags) {
            for (int i = 1; i <= 10; i++) { // 태그당 데이터 10개
                long id = allPosts.size() + 1L;
                allPosts.add(new PostDto(
                        titlePrefix + " " + i + " " + tag,
                        "#" + tag + " 관련된 설명입니다.",
                        "/images/sample" + (id % 5 + 1) + ".jpg",
                        rnd.nextInt(500),
                        Math.round((2 + rnd.nextDouble() * 3) * 10) / 10.0,
                        id,
                        category,
                        "여기는 상세 내용입니다.",
                        "작가" + id,
                        "서울시 마포구",
                        "평일 10:00 - 22:00",
                        "02-1234-5678"
                ));
            }
        }
    }

    private List<String> getTagsByCategory(String category) {
        if ("restaurant".equals(category)) {
            return List.of("식당", "한식", "일식", "중식", "분식", "고기집", "족발보쌈", "야시장", "포장마차");
        }
        return List.of("카페", "디저트", "브런치", "루프탑", "데이트", "혼밥", "술집", "바");
    }

    /**
     * 메인 페이지용 - 페이지 단위로 여러 태그 섹션 반환
     */
    public List<TagSection> getTagSectionsByCategoryPageAndSort(String category, int page, String sort) {
        int tagsPerPage = 5;
        List<String> tags = getTagsByCategory(category);
        int start = page * tagsPerPage;
        int end = Math.min(start + tagsPerPage, tags.size());

        List<TagSection> sections = new ArrayList<>();
        for (int i = start; i < end; i++) {
            String tag = tags.get(i);
            List<PostDto> posts = getPostsByCategoryAndTag(category, tag, sort);
            sections.add(new TagSection(tag, category, posts));
        }
        return sections;
    }

    /**
     * 메인 페이지용 - 단일 태그 섹션 반환
     */
    public List<TagSection> getTagSectionByTagAndSort(String category, String tag, String sort) {
        List<PostDto> posts = getPostsByCategoryAndTag(category, tag, sort);
        TagSection section = new TagSection(tag, category, posts);
        return List.of(section);
    }

    /**
     * 목록 페이지용 - 카테고리 + 태그 기반 게시물 리스트 반환
     */
    public List<PostDto> getPostsByCategoryAndTag(String category, String tag, String sort) {
        List<PostDto> filteredPosts = allPosts.stream()
                .filter(post -> post.getCategory().equals(category) && post.getDescription().contains("#" + tag))
                .collect(Collectors.toList());

        // 정렬
        switch (sort) {
            case "review":
                filteredPosts.sort(Comparator.comparingInt(PostDto::getReviewCount).reversed());
                break;
            case "rating":
                filteredPosts.sort(Comparator.comparingDouble(PostDto::getRating).reversed());
                break;
            default:
                break;
        }

        return filteredPosts;
    }

    /**
     * 상세 페이지용 - ID 기반 단일 게시물 반환
     */
    public PostDto getPostById(Long id) {
        return allPosts.stream()
                .filter(post -> post.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Long> getAllPostIds() {
        return allPosts.stream()
                .map(PostDto::getId)
                .collect(Collectors.toList());
    }
}
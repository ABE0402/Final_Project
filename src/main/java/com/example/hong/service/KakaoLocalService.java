package com.example.hong.service;

import com.example.hong.dto.KakaoSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class KakaoLocalService {

    private final RestTemplate kakaoRestTemplate;

    // 기존 메서드
    public KakaoSearchResponse searchKeyword(String query, double x, double y,
                                             Integer radius, String category,
                                             Integer size, String sort) {
        UriComponentsBuilder uri = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", query)
                .queryParam("x", x).queryParam("y", y);
        if (radius != null)   uri.queryParam("radius", radius);
        if (category != null) uri.queryParam("category_group_code", category);
        if (size != null)     uri.queryParam("size", size);
        if (sort != null)     uri.queryParam("sort", sort);
        return kakaoRestTemplate.getForObject(uri.toUriString(), KakaoSearchResponse.class);
    }

    // 좌표 보관용 레코드
    public record Coordinate(double lat, double lng) {}

    // (A) 주소 지오코딩: 도로명/지번 → 좌표
    public Coordinate geocodeByAddress(String roadAddress) {
        var uri = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", roadAddress)
                .queryParam("size", 1)
                .build(true);
        @SuppressWarnings("unchecked")
        var map = kakaoRestTemplate.getForObject(uri.toUriString(), java.util.Map.class);
        if (map == null) return null;
        var docs = (java.util.List<java.util.Map<String, Object>>) map.get("documents");
        if (docs == null || docs.isEmpty()) return null;

        var first = docs.get(0);
        try {
            double lng = Double.parseDouble(String.valueOf(first.get("x")));
            double lat = Double.parseDouble(String.valueOf(first.get("y")));
            return new Coordinate(lat, lng);
        } catch (Exception e) {
            return null;
        }
    }

    // (B) 키워드 지오코딩: 상호/지명 → 좌표
    public Coordinate geocodeFirst(String query) {
        var uri = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", query)
                .queryParam("size", 1)
                .build(true);
        var res = kakaoRestTemplate.getForObject(uri.toUriString(), KakaoSearchResponse.class);
        if (res == null || res.documents() == null || res.documents().isEmpty()) return null;
        var d = res.documents().get(0);
        try {
            return new Coordinate(Double.parseDouble(d.y()), Double.parseDouble(d.x()));
        } catch (Exception e) {
            return null;
        }
    }
}

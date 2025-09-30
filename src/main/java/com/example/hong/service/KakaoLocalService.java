package com.example.hong.service;

import com.example.hong.dto.KakaoSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoLocalService {

    private final RestTemplate kakaoRestTemplate;

    // 기존 메서드
    public KakaoSearchResponse searchKeyword(String query, double x, double y,
                                             Integer radius, String category,
                                             Integer size, String sort) {
        String q = sanitize(query);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", q)
                .queryParam("x", x)
                .queryParam("y", y);

        if (radius != null)   builder.queryParam("radius", radius);
        if (category != null) builder.queryParam("category_group_code", category);
        if (size != null)     builder.queryParam("size", size);
        if (sort != null)     builder.queryParam("sort", sort);

        var uri = builder.encode()
                .build()
                .toUri();

        try {
            return kakaoRestTemplate.getForObject(uri, KakaoSearchResponse.class);
        } catch (RestClientException e) {
            log.warn("Kakao searchKeyword failed. uri={}, err={}", uri, e.toString());
            return null;
        }
    }

    // 좌표 보관용 레코드
    public record Coordinate(double lat, double lng) {}

    // 주소 지오코딩 (도로명/지번 -> 좌표)
    public Coordinate geocodeByAddress(String roadAddress) {
        String q = sanitize(roadAddress);
        if (q == null) return null;

        var uri = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", q)
                .queryParam("size", 1)
                .encode()
                .build()
                .toUri();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = kakaoRestTemplate.getForObject(uri, Map.class);
            if (map == null) return null;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> docs = (List<Map<String, Object>>) map.get("documents");
            if (docs == null || docs.isEmpty()) return null;

            var first = docs.get(0);
            double lng = Double.parseDouble(String.valueOf(first.get("x")));
            double lat = Double.parseDouble(String.valueOf(first.get("y")));
            return new Coordinate(lat, lng);
        } catch (RestClientException | NumberFormatException e) {
            log.warn("Kakao geocodeByAddress failed. uri={}, err={}", uri, e.toString());
            return null;
        }
    }

    // 키워드 지오코딩: (상호/지명 → 좌표)
    public Coordinate geocodeFirst(String query) {
        String q = sanitize(query);
        if (q == null) return null;

        var uri = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", q)
                .queryParam("size", 1)
                .encode()
                .build()
                .toUri();

        try {
            var res = kakaoRestTemplate.getForObject(uri, KakaoSearchResponse.class);
            if (res == null || res.documents() == null || res.documents().isEmpty()) return null;

            var d = res.documents().get(0);
            return new Coordinate(Double.parseDouble(d.y()), Double.parseDouble(d.x()));
        } catch (RestClientException | NumberFormatException e) {
            log.warn("Kakao geocodeFirst failed. uri={}, err={}", uri, e.toString());
            return null;
        }
    }

    private String sanitize(String s){
        if (s == null) return null;
        String t = s.trim().replaceAll("\\s+", " ");
        return t.isEmpty() ? null : t;
    }
}

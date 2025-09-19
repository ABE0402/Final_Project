package com.example.mainproject.controller;

import com.example.mainproject.dto.KakaoSearchResponse;
import com.example.mainproject.service.KakaoLocalService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kakao")
public class KakaoController {

    private final KakaoLocalService svc;
    private final ObjectMapper mapper; // ← new ObjectMapper() 말고 주입 받기

    @GetMapping("/places")
    public KakaoSearchResponse places(@RequestParam String query,
                                      @RequestParam double x,
                                      @RequestParam double y,
                                      @RequestParam(defaultValue = "1000") Integer radius,
                                      @RequestParam(required=false, name="category") String category,
                                      @RequestParam(defaultValue = "15") Integer size,
                                      @RequestParam(defaultValue = "distance") String sort) {
        return svc.searchKeyword(query, x, y, radius, category, size, sort);
    }

    @GetMapping("/my-places")
    public List<Map<String, Object>> myPlaces(@RequestParam(required = false) String type) throws Exception {
        var res = new ClassPathResource("data/my-places.json");
        try (InputStream in = res.getInputStream()) {
            List<Map<String, Object>> all =
                    mapper.readValue(in, new TypeReference<List<Map<String, Object>>>() {});
            if (type == null || type.isBlank()) return all;
            return all.stream()
                    .filter(p -> type.equalsIgnoreCase(String.valueOf(p.getOrDefault("type", ""))))
                    .toList();
        }
    }

    // 키워드 지오코딩
    @GetMapping("/geocode")
    public Map<String, Object> geocode(@RequestParam("q") String q) {
        var c = svc.geocodeFirst(q);
        if (c == null) return Map.of("found", false);
        return Map.of("found", true, "lat", c.lat(), "lng", c.lng());
    }

    // 주소 지오코딩
    @GetMapping("/geocode/address")
    public Map<String, Object> geocodeByAddress(@RequestParam("address") String address) {
        var c = svc.geocodeByAddress(address);
        if (c == null) return Map.of("found", false, "address", address);
        return Map.of("found", true, "lat", c.lat(), "lng", c.lng());
    }
}

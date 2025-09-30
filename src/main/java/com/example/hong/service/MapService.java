package com.example.hong.service;

import com.example.hong.dto.MyPlaceDto;
import com.example.hong.repository.CafeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapService {

    private final CafeRepository cafeRepository;
    private final ObjectMapper objectMapper;

    public List<MyPlaceDto> getAllMapCafes() {
        List<MyPlaceDto> dbCafes = cafeRepository.findAll().stream()
                .map(MyPlaceDto::fromEntity)
                .collect(Collectors.toList());

        List<MyPlaceDto> jsonCafes = loadCafesFromJson();

        List<MyPlaceDto> allCafes = new ArrayList<>();
        allCafes.addAll(dbCafes);
        allCafes.addAll(jsonCafes);

        return allCafes;
    }

    private List<MyPlaceDto> loadCafesFromJson() {
        try {

            ClassPathResource resource = new ClassPathResource("static/data/my-places-with-coords.json");
            InputStream inputStream = resource.getInputStream();
            List<MyPlaceDto> cafes = objectMapper.readValue(inputStream, new TypeReference<>() {});

            // Json 데이터에 고유 번호를 붙여서 db에 id 값이랑 충돌방지
            cafes.forEach(cafe -> cafe.setId("json_" + cafe.getId()));

            return cafes;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
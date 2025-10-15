package com.example.hong.service;

import com.example.hong.document.PlaceDocument;
import com.example.hong.repository.PlaceSearchRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingService {

    private final PlaceSearchRepository placeSearchRepository;

    @PostConstruct
    public void indexCsvData() {
        if (placeSearchRepository.count() > 0) {
            log.info("Elasticsearch 'places' 인덱스에 이미 데이터가 존재합니다. 인덱싱을 건너뜁니다.");
            return;
        }

        log.info("CSV 데이터 인덱싱을 시작합니다...");
        ClassPathResource resource = new ClassPathResource("서울상권정보.csv");
        List<PlaceDocument> documents = new ArrayList<>();

        // 파일 인코딩은 UTF-8
        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream(), "UTF-8"))) {
            reader.readNext(); // 헤더 라인 건너뛰기

            String[] line;
            int lineNumber = 1;
            while ((line = reader.readNext()) != null) {
                lineNumber++;
                try {
                    if (line.length < 10) {
                        log.warn("{}번째 줄의 데이터가 부족하여 건너뜁니다. (컬럼 수: {})", lineNumber, line.length);
                        continue;
                    }

                    String idStr = line[0];   // 상가업소번호
                    String nameStr = line[1]; // 상호명
                    String lngStr = line[8]; // 경도
                    String latStr = line[9]; // 위도

                    if (idStr.isEmpty() || nameStr.isEmpty() || latStr.isEmpty() || lngStr.isEmpty()) {
                        log.warn("{}번째 줄에 필수 데이터(ID, 이름, 좌표)가 누락되어 건너뜁니다.", lineNumber);
                        continue;
                    }

                    // [수정됨] PlaceDocument 구조에 맞게 빌더 수정
                    PlaceDocument doc = PlaceDocument.builder()
                            .id(Long.parseLong(idStr))
                            .name(nameStr)
                            .address(line[5] + " " + line[7]) // 시군구명 + 법정동명
                            .category(line[4])                // 상권업종소분류명
                            .location(new GeoPoint(Double.parseDouble(latStr), Double.parseDouble(lngStr)))
                            .build();
                    documents.add(doc);

                } catch (Exception e) {
                    log.error("{}번째 줄 처리 중 오류 발생: {}", lineNumber, e.getMessage());
                }

                if (documents.size() >= 1000) {
                    placeSearchRepository.saveAll(documents);
                    documents.clear();
                    log.info("{}번째 줄까지 인덱싱 완료...", lineNumber);
                }
            }

            if (!documents.isEmpty()) {
                placeSearchRepository.saveAll(documents);
            }
            log.info("CSV 데이터 인덱싱 완료. 총 문서 수: {}", placeSearchRepository.count());

        } catch (IOException | CsvValidationException e) {
            log.error("CSV 파일 처리 중 심각한 오류 발생", e);
        }
    }
}
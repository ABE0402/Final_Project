package com.example.demo.controller;

import com.example.demo.dto.BusinessHours;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/store")
public class CommonApiController {
    // 실제 데이터는 DB에서 가져오는 로직으로 대체해야 합니다.
    // 여기서는 간단한 예시로 Map에 데이터를 저장했습니다.
    private Map<String, BusinessHours> getMockBusinessHours() {
        Map<String, BusinessHours> hours = new LinkedHashMap<>();
        hours.put("mon", new BusinessHours("12:00", "20:00", false));
        hours.put("tue", new BusinessHours("12:00", "20:00", false));
        hours.put("wed", new BusinessHours("12:00", "20:00", false));
        hours.put("thu", new BusinessHours("12:00", "20:00", false));
        hours.put("fri", new BusinessHours("12:00", "20:00", false));
        hours.put("sat", new BusinessHours("10:00", "22:00", false));
        hours.put("sun", new BusinessHours(null, null, true));
        return hours;
    }

    @GetMapping("/hours")
    public ResponseEntity<Map<String, BusinessHours>> getBusinessHours() {
        Map<String, BusinessHours> businessHours = getMockBusinessHours();
        return ResponseEntity.ok(businessHours);
    }
}

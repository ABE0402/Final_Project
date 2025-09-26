//package com.example.hong.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//
//import java.io.FileReader;
//
//@Service
//public class CafeImportService {
//    //CSV → 한 번에 두 테이블 insert
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    public void importCsv(String filePath) {
//        try (CSVReader reader = new CSVReader(new FileReader("cafes.csv"))) {
//            String[] line;
//            reader.readNext(); // header skip
//            while ((line = reader.readNext()) != null) {
//                String cafeName = line[1];
//                String address = line[11];
//                String phone = line[12];
//                String signatureMenu = line[3]; // signature_menu
//
//                // cafes insert
//                String sqlCafe = "INSERT INTO cafes (owner_user_id, name, address_road, phone, approval_status) VALUES (?, ?, ?, ?, 'APPROVED')";
//                jdbcTemplate.update(sqlCafe, 1, cafeName, address, phone);
//
//                // signature_menu → 여러 개면 분리
//                if (signatureMenu != null && !signatureMenu.isEmpty()) {
//                    String[] menus = signatureMenu.split(",");
//                    for (String m : menus) {
//                        String sqlMenu = "INSERT INTO cafe_menus (cafe_id, name) VALUES (LAST_INSERT_ID(), ?)";
//                        jdbcTemplate.update(sqlMenu, m.trim());
//                    }
//                }
//            }
//        }
//
//    }
//}
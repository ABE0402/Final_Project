package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private String title;
    private String description;
    private String thumbnailUrl;
    private int reviewCount;
    private double rating;
    private Long id;
    private String category;
    private String content;
    private String author;
    private String address;
    private String hours;
    private String contact;
}
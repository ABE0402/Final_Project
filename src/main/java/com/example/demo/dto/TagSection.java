package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class TagSection {
    private String tag;
    private String category;
    private List<PostDto> posts;
}
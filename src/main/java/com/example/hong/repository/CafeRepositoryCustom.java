package com.example.hong.repository;

import com.example.hong.entity.Cafe;
import com.example.hong.dto.SearchRequestDto;
import java.util.List;

public interface CafeRepositoryCustom {
    List<Cafe> search(SearchRequestDto condition, List<String> tagNames);
}
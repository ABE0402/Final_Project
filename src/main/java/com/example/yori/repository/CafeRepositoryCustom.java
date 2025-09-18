package com.example.yori.repository;

import com.example.yori.entity.Cafe;
import com.example.yori.dto.SearchRequestDto;
import java.util.List;

public interface CafeRepositoryCustom {
    List<Cafe> search(SearchRequestDto condition);
}
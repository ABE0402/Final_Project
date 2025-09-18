package com.example.yori.repository;


import com.example.yori.entity.SearchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchEventRepository extends JpaRepository<SearchEvent, Long> {
}
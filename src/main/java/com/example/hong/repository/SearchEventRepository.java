package com.example.hong.repository;


import com.example.hong.entity.SearchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchEventRepository extends JpaRepository<SearchEvent, Long> {
}
package com.example.hong.repository;

import com.example.hong.document.PlaceDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;
import java.util.List;

public interface PlaceSearchRepository extends ElasticsearchRepository<PlaceDocument, Long> {

    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name\", \"address\"]}}")
    List<PlaceDocument> searchByNameOrAddress(String keyword);
}
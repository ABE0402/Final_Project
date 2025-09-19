package com.example.mainproject.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoSearchResponse(
        @JsonProperty("meta") Meta meta,
        @JsonProperty("documents") List<Document> documents
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Meta(
            @JsonProperty("total_count") int totalCount,
            @JsonProperty("pageable_count") int pageableCount,
            @JsonProperty("is_end") boolean isEnd,
            @JsonProperty("same_name") SameName sameName
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record SameName(
                @JsonProperty("region") List<String> region,
                @JsonProperty("keyword") String keyword,
                @JsonProperty("selected_region") String selectedRegion
        ) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Document(
            @JsonProperty("id") String id,
            @JsonProperty("place_name") String placeName,
            @JsonProperty("category_name") String categoryName,
            @JsonProperty("category_group_code") String categoryGroupCode,
            @JsonProperty("category_group_name") String categoryGroupName,
            @JsonProperty("phone") String phone,
            @JsonProperty("address_name") String addressName,
            @JsonProperty("road_address_name") String roadAddressName,
            @JsonProperty("x") String x, // 경도(문자열)
            @JsonProperty("y") String y, // 위도(문자열)
            @JsonProperty("place_url") String placeUrl,
            @JsonProperty("distance") String distance // x,y 지정 시 반환
    ) {}
}

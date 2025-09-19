package com.example.mainproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KakaoClientConfig {

    @Bean
    public RestTemplate kakaoRestTemplate(
            RestTemplateBuilder builder,
            @Value("${kakao.rest-api-key}") String kakaoKey
    ) {
        return builder
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoKey)
                .build();
    }
}

package com.example.hong.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.time.Duration;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 예: file:/C:/Users/you/HONGAROUND/uploads/
        String fileUri = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        // 꼭 슬래시로 끝나도록 (Spring이 디렉터리로 인식)
        if (!fileUri.endsWith("/")) fileUri = fileUri + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileUri) // 예: file:/C:/.../uploads/
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());
    }
}

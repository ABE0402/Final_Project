package com.example.demo.controller;

import com.example.demo.dto.PostDto;
import com.example.demo.service.ReviewService;
import com.example.demo.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@AllArgsConstructor
public class CommonController {

    private final TagService tagService;
    private final ReviewService reviewService;

    // 상세 페이지를 모두 처리
    //@GetMapping("/{category}/{id}/detail")
    @GetMapping("/detail")
    public String detailPage(//@PathVariable String category,
                             //@PathVariable Long id,
                             @RequestParam String category,
                             @RequestParam Long id,
                             Model model) {

        // TagService를 통해 ID로 게시물 정보 가져오기
        PostDto post = tagService.getPostById(id);

        if (post == null) {
            return "notFoundPage"; // 게시물이 없을 경우 404 페이지 반환
        }

        // PostDto의 description에서 태그를 추출하여 리스트로 만듭니다.
        List<String> tags = new ArrayList<>();
        String description = post.getDescription();
        if (description != null) {
            Pattern pattern = Pattern.compile("#(\\S+)");
            Matcher matcher = pattern.matcher(description);
            while (matcher.find()) {
                tags.add(matcher.group(1)); // #을 제외한 태그 이름만 추출
            }
        }

        model.addAttribute("post", post);
        model.addAttribute("reviews", reviewService.getReviewsByPlaceId(id));
        model.addAttribute("tags", tags); // 추출한 태그 리스트를 모델에 추가);
        model.addAttribute("category", category);

        model.addAttribute("averageRating", 1.7);

        return "detail"; // detail.html 템플릿 반환
    }
}

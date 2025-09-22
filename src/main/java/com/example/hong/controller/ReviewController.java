package com.example.hong.controller;

import com.example.hong.dto.ReviewDto;
import com.example.hong.service.ReviewService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    //@GetMapping("/{category}/{postId}/reviews")
    @GetMapping("/reviews")
    public String goToReviewPage (//@PathVariable String category,
                                  //@PathVariable Long postId,
                                  @RequestParam String category,
                                  @RequestParam Long postId,
                                  Model model) {
        model.addAttribute("placeId", postId);
        return "review-list";
    }

    //@GetMapping("/{category}/{postId}/reviews/write")
    @GetMapping("/reviews/write")
    public String goToReviewWritePage (//@PathVariable String category,
                                       //@PathVariable Long postId,
                                       @RequestParam String category,
                                       @RequestParam Long postId,
                                       Model model) {
        log.info(postId.toString());
        return "review-write";
    }

    //@PostMapping("{category}/{postId}/submit")
    @PostMapping("/reviews/submit")
    public String submitReview(//@PathVariable String category,
                               //@PathVariable Long postId,
                               @RequestParam String category,
                               @RequestParam Long postId,
                               ReviewDto reviewDto) {
        reviewService.saveReview(reviewDto.getPlaceId(), reviewDto);
        return "redirect:/reviews/" + category + "/" + postId;
    }

}
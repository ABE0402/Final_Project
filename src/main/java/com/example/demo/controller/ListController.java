package com.example.demo.controller;

import com.example.demo.dto.PostDto;
import com.example.demo.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class ListController {

    private final TagService tagService;

    //@GetMapping("/{category}/list")
    @GetMapping("/list")
    public String getTaggedList(//@PathVariable String category,
                                @RequestParam String category,
                                @RequestParam String tag,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "recommend") String sort,
                                Model model) {

        List<PostDto> taggedPosts = tagService.getPostsByCategoryAndTag(category, tag, sort);

        model.addAttribute("category", category);
        model.addAttribute("tag", tag);
        model.addAttribute("posts", taggedPosts);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSort", sort);

        return "list";
    }
}
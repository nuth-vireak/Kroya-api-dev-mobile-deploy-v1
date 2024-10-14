package com.kshrd.kroya_api.controller;

import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.Category.CategoryRequest;
import com.kshrd.kroya_api.service.Category.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/category")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/post-category")
    public BaseResponse<?> postCategory(@RequestBody CategoryRequest categoryRequest){
        return categoryService.postCategory(categoryRequest);
    }

    @GetMapping("/all")
    public BaseResponse<?> getAllCategory(){
        return categoryService.getAllCategory();
    }
}

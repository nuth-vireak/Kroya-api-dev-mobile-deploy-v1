package com.kshrd.kroya_api.controller;

import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.service.Foods.FoodsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/foods")
@RequiredArgsConstructor
@Slf4j
public class FoodsController {

    private final FoodsService foodsService;

    @GetMapping("/{categoryId}")
    public BaseResponse<?> getAllFoodsByCategory(@PathVariable Long categoryId) {
        return foodsService.getAllFoodsByCategory(categoryId);
    }

    @GetMapping("/popular")
    public BaseResponse<?> getPopularFoods() {
        return foodsService.getPopularFoods();
    }
}

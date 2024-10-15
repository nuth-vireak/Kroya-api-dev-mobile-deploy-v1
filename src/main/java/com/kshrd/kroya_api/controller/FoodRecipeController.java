package com.kshrd.kroya_api.controller;

import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.FoodRecipe.FoodRecipeRequest;
import com.kshrd.kroya_api.service.FoodRecipe.FoodRecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/food-recipe")
@RequiredArgsConstructor
@Slf4j
public class FoodRecipeController {

    private final FoodRecipeService foodRecipeService;

    @PostMapping("/post-food-recipe")
    public BaseResponse<?> createRecipe(@RequestBody FoodRecipeRequest foodRecipeRequest) {
        return foodRecipeService.createRecipe(foodRecipeRequest);
    }

    @GetMapping("/list")
    public BaseResponse<?> getAllFoodSells() {
        return foodRecipeService.getAllFoodRecipes();
    }
}

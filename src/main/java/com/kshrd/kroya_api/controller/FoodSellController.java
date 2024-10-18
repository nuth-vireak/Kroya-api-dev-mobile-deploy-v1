package com.kshrd.kroya_api.controller;

import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.FoodRecipe.FoodRecipeRequest;
import com.kshrd.kroya_api.payload.FoodSell.FoodSellRequest;
import com.kshrd.kroya_api.service.FoodSell.FoodSellService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/food-sell")
@RequiredArgsConstructor
@Slf4j
public class FoodSellController {

    private final FoodSellService foodSellService;

    @SecurityRequirement(name="bearerAuth")
    @PostMapping("/post-food-sell")
    public BaseResponse<?> createRecipe(@RequestBody FoodSellRequest foodRecipeRequest, @RequestParam Long foodRecipeId) {
        return foodSellService.createFoodSell(foodRecipeRequest, foodRecipeId);
    }

    @GetMapping("/list")
    public BaseResponse<?> getAllFoodSells() {
        return foodSellService.getAllFoodSells();
    }

}
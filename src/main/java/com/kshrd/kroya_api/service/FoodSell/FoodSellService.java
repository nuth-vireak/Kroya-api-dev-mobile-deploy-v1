package com.kshrd.kroya_api.service.FoodSell;

import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.FoodSell.FoodSellRequest;

public interface FoodSellService {
    BaseResponse<?> createFoodSell(FoodSellRequest foodRecipeRequest, Long foodRecipeId);

    BaseResponse<?> getAllFoodSells();
}

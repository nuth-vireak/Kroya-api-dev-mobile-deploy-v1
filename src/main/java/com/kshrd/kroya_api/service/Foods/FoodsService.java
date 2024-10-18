package com.kshrd.kroya_api.service.Foods;

import com.kshrd.kroya_api.payload.BaseResponse;

public interface FoodsService {
    BaseResponse<?> getAllFoodsByCategory(Long categoryId);

    BaseResponse<?> getPopularFoods();
}


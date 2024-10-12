package com.kshrd.kroya_api.service.Recipe;

import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.Recipe.RecipeRequest;

public interface RecipeService {
    BaseResponse createRecipe(RecipeRequest recipeRequest);
}

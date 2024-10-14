package com.kshrd.kroya_api.payload.FoodRecipe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodRecipeRequest {
    private String photoUrl;
    private String name;
    private String description;
    private Integer durationInMinutes;
    private String level;
    private Long cuisineId;
    private Long categoryId;
    private List<Ingredient> ingredients;
    private List<CookingStep> cookingSteps;
    private Boolean isForSale;
}

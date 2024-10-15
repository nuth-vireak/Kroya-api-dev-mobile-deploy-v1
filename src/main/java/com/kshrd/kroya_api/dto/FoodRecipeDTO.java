package com.kshrd.kroya_api.dto;

import com.kshrd.kroya_api.payload.FoodRecipe.CookingStep;
import com.kshrd.kroya_api.payload.FoodRecipe.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodRecipeDTO {
    private Long id;
    private String photoUrl;
    private String name;
    private String description;
    private Integer durationInMinutes;
    private String level;
    private List<Ingredient> ingredients;
    private List<CookingStep> cookingSteps;
    private LocalDateTime createdAt;
    private UserDTO user;
}
package com.kshrd.kroya_api.payload.FoodRecipe;

import com.kshrd.kroya_api.dto.UserDTO;
import com.kshrd.kroya_api.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodRecipeResponse {
    private Integer id;
    private String photoUrl;
    private String name;
    private String description;
    private Integer durationInMinutes;
    private String level;
    private String cuisineName;
    private String categoryName;
    private List<Ingredient> ingredients;
    private List<CookingStep> cookingSteps;
    private ItemType itemType = ItemType.FOOD_RECIPE;
    private UserDTO user;
    private LocalDateTime createdAt;
}

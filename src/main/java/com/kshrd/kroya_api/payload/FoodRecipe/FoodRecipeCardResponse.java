package com.kshrd.kroya_api.payload.FoodRecipe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodRecipeCardResponse {
    private Long id;
    private String photoUrl;
    private String name;
    private String description;
    private Double price;
    private String level;
    private Double averageRating;
    private Integer totalRaters;
    private Boolean isFavorite;
}

package com.kshrd.kroya_api.payload.Recipe;

import com.kshrd.kroya_api.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeResponse {
    private Integer id;
    private String photoUrl;
    private String name;
    private String description;
    private Integer durationInMinutes;
    private String level;
    private String cuisine;
    private String category;
    private List<Ingredient> ingredients;
    private String cookingSteps;
    private Boolean isForSale;
    private LocalDateTime createdAt;
    private UserDTO user;
}

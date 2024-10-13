package com.kshrd.kroya_api.service.Recipe;

import com.kshrd.kroya_api.entity.RecipeEntity;
import com.kshrd.kroya_api.entity.UserEntity;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.Recipe.RecipeRequest;
import com.kshrd.kroya_api.payload.Recipe.RecipeResponse;
import com.kshrd.kroya_api.repository.Recipe.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeServerImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final ModelMapper modelMapper;

    @Override
    public BaseResponse createRecipe(RecipeRequest recipeRequest) {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("User authenticated: {}", currentUser.getEmail());

        // Map the RecipeRequest to RecipeEntity
        RecipeEntity recipeEntity = RecipeEntity.builder()
                .photoUrl(recipeRequest.getPhotoUrl())
                .name(recipeRequest.getName())
                .description(recipeRequest.getDescription())
                .durationInMinutes(recipeRequest.getDurationInMinutes())
                .level(recipeRequest.getLevel())
                .cuisine(recipeRequest.getCuisine())
                .category(recipeRequest.getCategory())
                .ingredients(recipeRequest.getIngredients())
                .cookingSteps(recipeRequest.getCookingSteps())
                .isForSale(recipeRequest.getIsForSale())
                .user(currentUser)
                .createdAt(LocalDateTime.now())
                .build();

        // Log before saving the recipe
        log.info("Saving recipe: {}", recipeRequest.getName());

        // Save the recipe to the database
        RecipeEntity savedRecipe = recipeRepository.save(recipeEntity);

        // Log the newly created recipe's ID
        log.info("Recipe saved successfully with ID: {}", savedRecipe.getId());

        // Map the saved entity to RecipeResponse using ModelMapper
        RecipeResponse recipeResponse = modelMapper.map(savedRecipe, RecipeResponse.class);

        // Log the response being sent back
        log.info("Recipe Response - ID: {}, Name: {}", recipeResponse.getId(), recipeResponse.getName());

        // Return a success response with the saved recipe as the payload
        return BaseResponse.builder()
                .payload(recipeResponse)
                .message("Recipe created successfully")
                .statusCode("201")
                .build();
    }
}

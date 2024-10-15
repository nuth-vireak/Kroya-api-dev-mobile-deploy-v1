package com.kshrd.kroya_api.service.FoodRecipe;

import com.kshrd.kroya_api.entity.*;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.FoodRecipe.*;
import com.kshrd.kroya_api.repository.Category.CategoryRepository;
import com.kshrd.kroya_api.repository.Cuisine.CuisineRepository;
import com.kshrd.kroya_api.repository.Favorite.FavoriteRepository;
import com.kshrd.kroya_api.repository.FoodRecipe.FoodRecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodRecipeServiceImpl implements FoodRecipeService {

    private final FoodRecipeRepository foodRecipeRepository;
    private final CategoryRepository categoryRepository;
    private final CuisineRepository cuisineRepository;
    private final FavoriteRepository favoriteRepository;
    private final ModelMapper modelMapper;

    @Override
    public BaseResponse<?> createRecipe(FoodRecipeRequest foodRecipeRequest) {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("User authenticated: {}", currentUser.getEmail());

        // Fetch CategoryEntity by category ID
        Optional<CategoryEntity> categoryOptional = categoryRepository.findById(foodRecipeRequest.getCategoryId());
        if (categoryOptional.isEmpty()) {
            log.error("Category with ID {} not found", foodRecipeRequest.getCategoryId());
            return BaseResponse.builder()
                    .message("Category not found")
                    .statusCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
                    .build();
        }
        CategoryEntity categoryEntity = categoryOptional.get();

        // Fetch CuisineEntity by cuisine ID
        Optional<CuisineEntity> cuisineOptional = cuisineRepository.findById(foodRecipeRequest.getCuisineId());
        if (cuisineOptional.isEmpty()) {
            log.error("Cuisine with ID {} not found", foodRecipeRequest.getCuisineId());
            return BaseResponse.builder()
                    .message("Cuisine not found")
                    .statusCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
                    .build();
        }

        CuisineEntity cuisineEntity = cuisineOptional.get();

        // Assign unique IDs to ingredients if they are null or 0
        List<Ingredient> ingredients = foodRecipeRequest.getIngredients();
        long ingredientIdCounter = 1L;  // Start the ID counter for ingredients

        for (Ingredient ingredient : ingredients) {
            if (ingredient.getId() == null || ingredient.getId() == 0) {
                ingredient.setId(ingredientIdCounter++);  // Assign a new ID
            }
        }

        // Assign unique IDs to cooking steps if they are null or 0
        List<CookingStep> cookingSteps = foodRecipeRequest.getCookingSteps();
        long cookingStepIdCounter = 1L;  // Start the ID counter for cooking steps

        for (CookingStep cookingStep : cookingSteps) {
            if (cookingStep.getId() == null || cookingStep.getId() == 0) {
                cookingStep.setId(cookingStepIdCounter++);  // Assign a new ID
            }
        }

        // Map the RecipeRequest to RecipeEntity
        FoodRecipeEntity foodRecipeEntity = FoodRecipeEntity.builder()
                .photoUrl(foodRecipeRequest.getPhotoUrl())
                .name(foodRecipeRequest.getName())
                .description(foodRecipeRequest.getDescription())
                .durationInMinutes(foodRecipeRequest.getDurationInMinutes())
                .level(foodRecipeRequest.getLevel())
                .cuisine(cuisineEntity)  // Set CuisineEntity (using ID)
                .category(categoryEntity)  // Set CategoryEntity (using ID)
                .ingredients(ingredients)
                .cookingSteps(cookingSteps)
                .user(currentUser)
                .createdAt(LocalDateTime.now())
                .build();

        // Log before saving the recipe
        log.info("Saving recipe: {}", foodRecipeRequest.getName());

        // Save the recipe to the database
        FoodRecipeEntity savedRecipe = foodRecipeRepository.save(foodRecipeEntity);

        // Log the newly created recipe's ID
        log.info("Recipe saved successfully with ID: {}", savedRecipe.getId());

        // Map the saved entity to RecipeResponse using ModelMapper
        FoodRecipeResponse foodRecipeResponse = modelMapper.map(savedRecipe, FoodRecipeResponse.class);

        // Log the response being sent back
        log.info("Recipe Response - ID: {}, Name: {}", foodRecipeResponse.getId(), foodRecipeResponse.getName());

        // Set category name in the response
        foodRecipeResponse.setCategoryName(categoryEntity.getCategoryName());

        // Set cuisine name in the response
        foodRecipeResponse.setCuisineName(cuisineEntity.getCuisineName());

        // Return a success response with the saved recipe as the payload
        return BaseResponse.builder()
                .payload(foodRecipeResponse)
                .message("Recipe created successfully")
                .statusCode("201")
                .build();
    }

    @Override
    public BaseResponse<?> getAllFoodRecipes() {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("User authenticated: {}", currentUser.getEmail());

        // Check if the user has the role ROLE_USER or ROLE_GUEST
        boolean isUser = currentUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_USER"));
        boolean isGuest = currentUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_GUEST"));

        // Fetch all FoodRecipeEntity records from the database
        List<FoodRecipeEntity> foodRecipeEntities = foodRecipeRepository.findAll();

        // For ROLE_USER: Fetch the user's favorite recipes
        List<Long> userFavoriteRecipeIds;
        if (isUser) {
            // Fetch the user's favorite recipes
            List<FavoriteEntity> userFavorites = favoriteRepository.findByUserAndFoodRecipeIsNotNull(currentUser);
            userFavoriteRecipeIds = userFavorites.stream()
                    .map(favorite -> favorite.getFoodRecipe().getId())
                    .toList();
        } else {
            userFavoriteRecipeIds = new ArrayList<>();
        }

        // Map each FoodRecipeEntity to FoodRecipeCardResponse using ModelMapper
        List<FoodRecipeCardResponse> foodRecipeCardResponses = foodRecipeEntities.stream()
                .map(foodRecipeEntity -> {
                    // Map using ModelMapper
                    FoodRecipeCardResponse response = modelMapper.map(foodRecipeEntity, FoodRecipeCardResponse.class);

                    // For ROLE_USER, check if the recipe is in the user's favorite list
                    if (isUser && userFavoriteRecipeIds.contains(foodRecipeEntity.getId())) {
                        response.setIsFavorite(true);
                    } else if (isGuest) {
                        // For ROLE_GUEST, always set isFavorite to false
                        response.setIsFavorite(false);
                    } else {
                        // For non-favorite recipes for a regular user
                        response.setIsFavorite(false);
                    }

                    // Additional logic for price if applicable (e.g., FoodSellEntity relationship)
                    // response.setPrice(foodRecipeEntity.getFoodSell() != null ? foodRecipeEntity.getFoodSell().getPrice() : null);

                    return response;
                })
                .toList();

        // Return the response with the list of FoodRecipeCardResponse objects
        return BaseResponse.builder()
                .message("All food recipes fetched successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .payload(foodRecipeCardResponses)
                .build();
    }
}

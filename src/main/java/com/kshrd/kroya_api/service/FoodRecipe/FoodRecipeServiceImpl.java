package com.kshrd.kroya_api.service.FoodRecipe;

import com.kshrd.kroya_api.entity.*;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.FoodRecipe.*;
import com.kshrd.kroya_api.dto.PhotoDTO;
import com.kshrd.kroya_api.repository.Category.CategoryRepository;
import com.kshrd.kroya_api.repository.Cuisine.CuisineRepository;
import com.kshrd.kroya_api.repository.Favorite.FavoriteRepository;
import com.kshrd.kroya_api.repository.FoodRecipe.FoodRecipeRepository;
import com.kshrd.kroya_api.repository.FoodSell.FoodSellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodRecipeServiceImpl implements FoodRecipeService {

    private final FoodRecipeRepository foodRecipeRepository;
    private final FoodSellRepository foodSellRepository;
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

        // Save the recipe to the database first
        FoodRecipeEntity savedRecipe = foodRecipeRepository.save(foodRecipeEntity);

        // Process photo entities from the request
        List<PhotoEntity> photoEntities = foodRecipeRequest.getPhoto().stream()
                .map(photoEntity -> {
                    photoEntity.setFoodRecipe(savedRecipe); // Set the association with the saved recipe
                    return photoEntity;
                })
                .collect(Collectors.toList());

        // Set the photos to the saved recipe
        savedRecipe.setPhotos(photoEntities);

        // Save the recipe again to persist the photos
        foodRecipeRepository.save(savedRecipe);

        // Log the newly created recipe's ID
        log.info("Recipe saved successfully with ID: {}", savedRecipe.getId());

        // Map the saved entity to RecipeResponse using ModelMapper
        FoodRecipeResponse foodRecipeResponse = modelMapper.map(savedRecipe, FoodRecipeResponse.class);

        // Map photo entities to PhotoResponse objects
        List<PhotoDTO> photoRespons = savedRecipe.getPhotos().stream()
                .map(photoEntity -> new PhotoDTO(photoEntity.getId(), photoEntity.getPhoto()))
                .collect(Collectors.toList());
        foodRecipeResponse.setPhoto(photoRespons);

        // Set category name in the response
        foodRecipeResponse.setCategoryName(categoryEntity.getCategoryName());

        // Set cuisine name in the response
        foodRecipeResponse.setCuisineName(cuisineEntity.getCuisineName());

        // Check if this recipe is a favorite for the current user
        boolean isFavorite = favoriteRepository.existsByUserAndFoodRecipe(currentUser, savedRecipe);
        foodRecipeResponse.setIsFavorite(isFavorite);

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

        // Fetch all FoodRecipeEntity records from the database
        List<FoodRecipeEntity> foodRecipeEntities = foodRecipeRepository.findAll();

        // Fetch the user's favorite recipes
        List<FavoriteEntity> userFavorites = favoriteRepository.findByUserAndFoodRecipeIsNotNull(currentUser);
        List<Long> userFavoriteRecipeIds = userFavorites.stream()
                .map(favorite -> favorite.getFoodRecipe().getId())
                .toList();

        // Filter out FoodRecipeEntities that have a related FoodSellEntity
        List<FoodRecipeCardResponse> foodRecipeResponses = foodRecipeEntities.stream()
                .filter(foodRecipeEntity -> !foodSellRepository.existsByFoodRecipe(foodRecipeEntity))
                .map(foodRecipeEntity -> {
                    // Map to FoodRecipeCardResponse using ModelMapper
                    FoodRecipeCardResponse response = modelMapper.map(foodRecipeEntity, FoodRecipeCardResponse.class);

                    // Set isFavorite if it's in the user's favorites
                    response.setIsFavorite(userFavoriteRecipeIds.contains(foodRecipeEntity.getId()));

                    // Map photos to PhotoResponse
                    List<PhotoDTO> photoResponses = foodRecipeEntity.getPhotos().stream()
                            .map(photoEntity -> new PhotoDTO(photoEntity.getId(), photoEntity.getPhoto()))
                            .collect(Collectors.toList());

                    // Set the photo field in the response
                    response.setPhoto(photoResponses);

                    return response;
                })
                .toList();

        // Return the response with the list of FoodRecipeCardResponse objects
        return BaseResponse.builder()
                .message("All food recipes fetched successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .payload(foodRecipeResponses)
                .build();
    }



}

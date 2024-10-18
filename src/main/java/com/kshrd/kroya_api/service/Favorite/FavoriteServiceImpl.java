package com.kshrd.kroya_api.service.Favorite;

import com.kshrd.kroya_api.dto.PhotoDTO;
import com.kshrd.kroya_api.entity.*;
import com.kshrd.kroya_api.enums.ItemType;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.FoodRecipe.FoodRecipeCardResponse;
import com.kshrd.kroya_api.payload.FoodSell.FoodSellCardResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final FoodRecipeRepository foodRecipeRepository;
    private final FoodSellRepository foodSellRepository;
    private final ModelMapper modelMapper;

    @Override
    public BaseResponse<?> saveFoodToFavorite(Long foodId, ItemType itemType) {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("User authenticated: {}", currentUser.getEmail());

        if (itemType == ItemType.FOOD_RECIPE) {
            // Find the FoodRecipe by ID
            Optional<FoodRecipeEntity> foodRecipeOptional = foodRecipeRepository.findById(Math.toIntExact(foodId));
            if (foodRecipeOptional.isEmpty()) {
                return BaseResponse.builder()
                        .message("Food Recipe not found")
                        .statusCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
                        .build();
            }

            FoodRecipeEntity foodRecipe = foodRecipeOptional.get();

            // Check if this food recipe is linked to any food sell
            Optional<FoodSellEntity> linkedSell = foodSellRepository.findByFoodRecipe(foodRecipe);
            if (linkedSell.isPresent()) {
                return BaseResponse.builder()
                        .message("This recipe is part of a Food Sell. Please select the Food Sell item instead.")
                        .statusCode("400")
                        .build();
            }

            // Check if the recipe is already in the user's favorites
            Optional<FavoriteEntity> existingFavorite = favoriteRepository.findByUserAndFoodRecipe(currentUser, foodRecipe);
            if (existingFavorite.isPresent()) {
                return BaseResponse.builder()
                        .message("This recipe is already in your favorites")
                        .statusCode("400")
                        .build();
            }

            // Save the recipe to the favorites
            FavoriteEntity favoriteEntity = FavoriteEntity.builder()
                    .user(currentUser)
                    .foodRecipe(foodRecipe)
                    .favoriteDate(LocalDateTime.now())
                    .build();

            favoriteRepository.save(favoriteEntity);

        } else if (itemType == ItemType.FOOD_SELL) {
            // Find the FoodSell by ID
            Optional<FoodSellEntity> foodSellOptional = foodSellRepository.findById(foodId);
            if (foodSellOptional.isEmpty()) {
                return BaseResponse.builder()
                        .message("Food Sell not found")
                        .statusCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
                        .build();
            }

            FoodSellEntity foodSell = foodSellOptional.get();

            // Check if the sell item is already in the user's favorites
            Optional<FavoriteEntity> existingFavorite = favoriteRepository.findByUserAndFoodSell(currentUser, foodSell);
            if (existingFavorite.isPresent()) {
                return BaseResponse.builder()
                        .message("This item is already in your favorites")
                        .statusCode("400")
                        .build();
            }

            // Save the sell item to the favorites
            FavoriteEntity favoriteEntity = FavoriteEntity.builder()
                    .user(currentUser)
                    .foodSell(foodSell)
                    .favoriteDate(LocalDateTime.now())
                    .build();

            favoriteRepository.save(favoriteEntity);

        } else {
            return BaseResponse.builder()
                    .message("Invalid item type")
                    .statusCode("400")
                    .build();
        }

        return BaseResponse.builder()
                .message("Item added to favorites")
                .statusCode("201")
                .build();
    }


    @Override
    public BaseResponse<?> unsavedFoodFromFavorite(Long foodId, ItemType itemType) {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("User authenticated: {}", currentUser.getEmail());

        if (itemType == ItemType.FOOD_RECIPE) {
            // Find the FoodRecipe by ID
            Optional<FoodRecipeEntity> foodRecipeOptional = foodRecipeRepository.findById(Math.toIntExact(foodId));
            if (foodRecipeOptional.isEmpty()) {
                return BaseResponse.builder()
                        .message("Food Recipe not found")
                        .statusCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
                        .build();
            }

            FoodRecipeEntity foodRecipe = foodRecipeOptional.get();

            // Check if this food recipe is linked to any food sell
            Optional<FoodSellEntity> linkedSell = foodSellRepository.findByFoodRecipe(foodRecipe);
            if (linkedSell.isPresent()) {
                return BaseResponse.builder()
                        .message("This recipe is part of a Food Sell. Please select the Food Sell item instead.")
                        .statusCode("400")
                        .build();
            }

            // Check if the recipe is in the user's favorites
            Optional<FavoriteEntity> existingFavorite = favoriteRepository.findByUserAndFoodRecipe(currentUser, foodRecipe);
            if (existingFavorite.isEmpty()) {
                return BaseResponse.builder()
                        .message("This recipe is not in your favorites")
                        .statusCode("400")
                        .build();
            }

            // Remove the favorite entry
            favoriteRepository.delete(existingFavorite.get());
            log.info("Food Recipe removed from favorites for user: {}", currentUser.getEmail());

        } else if (itemType == ItemType.FOOD_SELL) {
            // Find the FoodSell by ID
            Optional<FoodSellEntity> foodSellOptional = foodSellRepository.findById(foodId);
            if (foodSellOptional.isEmpty()) {
                return BaseResponse.builder()
                        .message("Food Sell not found")
                        .statusCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
                        .build();
            }

            FoodSellEntity foodSell = foodSellOptional.get();

            // Check if the sell item is in the user's favorites
            Optional<FavoriteEntity> existingFavorite = favoriteRepository.findByUserAndFoodSell(currentUser, foodSell);
            if (existingFavorite.isEmpty()) {
                return BaseResponse.builder()
                        .message("This item is not in your favorites")
                        .statusCode("400")
                        .build();
            }

            // Remove the favorite entry
            favoriteRepository.delete(existingFavorite.get());
            log.info("Food Sell removed from favorites for user: {}", currentUser.getEmail());

        } else {
            return BaseResponse.builder()
                    .message("Invalid item type")
                    .statusCode("400")
                    .build();
        }

        return BaseResponse.builder()
                .message("Item removed from favorites")
                .statusCode("200")
                .build();
    }

    @Override
    public BaseResponse<?> getAllFavoriteFoodsByCurrentUser() {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Fetching favorite foods for user: {}", currentUser.getEmail());

        // Fetch all favorite entities for the user
        List<FavoriteEntity> favoriteEntities = favoriteRepository.findByUser(currentUser);

        // Map favorite entities to FoodRecipeCardResponse (pure food recipes)
        List<FoodRecipeCardResponse> favoriteFoodRecipes = favoriteEntities.stream()
                .filter(favorite -> favorite.getFoodRecipe() != null && favorite.getFoodSell() == null) // Only pure recipes
                .map(favorite -> {
                    FoodRecipeCardResponse response = modelMapper.map(favorite.getFoodRecipe(), FoodRecipeCardResponse.class);
                    response.setIsFavorite(true);  // Mark it as favorite

                    // Map photos from FoodRecipeEntity to structured list
                    List<PhotoDTO> photoDTOs = favorite.getFoodRecipe().getPhotos().stream()
                            .map(photo -> new PhotoDTO(photo.getId(), photo.getPhoto()))
                            .collect(Collectors.toList());
                    response.setPhoto(photoDTOs);

                    return response;
                })
                .toList();

        // Map favorite entities to FoodSellCardResponse (food sells)
        List<FoodSellCardResponse> favoriteFoodSells = favoriteEntities.stream()
                .filter(favorite -> favorite.getFoodSell() != null) // Only sells
                .map(favorite -> {
                    FoodSellCardResponse response = modelMapper.map(favorite.getFoodSell(), FoodSellCardResponse.class);

                    // Set additional fields from the related FoodRecipeEntity
                    FoodRecipeEntity linkedRecipe = favorite.getFoodSell().getFoodRecipe();

                    // Map photos from FoodRecipeEntity to structured list
                    List<PhotoDTO> photoDTOs = linkedRecipe.getPhotos().stream()
                            .map(photo -> new PhotoDTO(photo.getId(), photo.getPhoto()))
                            .collect(Collectors.toList());
                    response.setPhoto(photoDTOs);

                    response.setName(linkedRecipe.getName());
                    response.setAverageRating(linkedRecipe.getAverageRating());
                    response.setTotalRaters(linkedRecipe.getTotalRaters());
                    response.setIsFavorite(true);

                    return response;
                })
                .toList();

        // Prepare the response map
        Map<String, List<?>> responseMap = new HashMap<>();
        responseMap.put("favoriteFoodRecipes", favoriteFoodRecipes);
        responseMap.put("favoriteFoodSells", favoriteFoodSells);

        // Build the BaseResponse
        return BaseResponse.builder()
                .message("Favorite foods fetched successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .payload(responseMap)
                .build();
    }


}

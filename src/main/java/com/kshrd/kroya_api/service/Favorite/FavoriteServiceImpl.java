package com.kshrd.kroya_api.service.Favorite;

import com.kshrd.kroya_api.entity.FavoriteEntity;
import com.kshrd.kroya_api.entity.FoodRecipeEntity;
import com.kshrd.kroya_api.entity.FoodSellEntity;
import com.kshrd.kroya_api.entity.UserEntity;
import com.kshrd.kroya_api.enums.ItemType;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.repository.Favorite.FavoriteRepository;
import com.kshrd.kroya_api.repository.FoodRecipe.FoodRecipeRepository;
import com.kshrd.kroya_api.repository.FoodSell.FoodSellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final FoodRecipeRepository foodRecipeRepository;
    private final FoodSellRepository foodSellRepository;

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
}

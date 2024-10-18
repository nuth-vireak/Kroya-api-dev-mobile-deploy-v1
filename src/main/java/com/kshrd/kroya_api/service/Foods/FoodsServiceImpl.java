package com.kshrd.kroya_api.service.Foods;

import com.kshrd.kroya_api.dto.PhotoDTO;
import com.kshrd.kroya_api.entity.FoodRecipeEntity;
import com.kshrd.kroya_api.entity.FoodSellEntity;
import com.kshrd.kroya_api.entity.PhotoEntity;
import com.kshrd.kroya_api.entity.UserEntity;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.FoodRecipe.FoodRecipeCardResponse;
import com.kshrd.kroya_api.payload.FoodSell.FoodSellCardResponse;
import com.kshrd.kroya_api.repository.Category.CategoryRepository;
import com.kshrd.kroya_api.repository.Favorite.FavoriteRepository;
import com.kshrd.kroya_api.repository.FoodRecipe.FoodRecipeRepository;
import com.kshrd.kroya_api.repository.FoodSell.FoodSellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodsServiceImpl implements FoodsService {

    private final FoodRecipeRepository foodRecipeRepository;
    private final FoodSellRepository foodSellRepository;
    private final FavoriteRepository favoriteRepository;
    private final ModelMapper modelMapper;

    @Override
    public BaseResponse<?> getAllFoodsByCategory(Long categoryId) {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("User authenticated: {}", currentUser.getEmail());

        // Fetch all food recipes by category
        List<FoodRecipeEntity> foodRecipes = foodRecipeRepository.findByCategoryId(categoryId);
        List<Long> userFavoriteRecipeIds = favoriteRepository.findByUserAndFoodRecipeIsNotNull(currentUser)
                .stream()
                .map(favorite -> favorite.getFoodRecipe().getId())
                .toList();

        // Filter out food recipes that are linked to food sells
        List<FoodRecipeEntity> pureFoodRecipes = foodRecipes.stream()
                .filter(recipe -> foodSellRepository.findByFoodRecipe(recipe).isEmpty())
                .collect(Collectors.toList());

        // Fetch all food sells by category
        List<FoodSellEntity> foodSells = foodSellRepository.findByCategoryId(categoryId);
        List<Long> userFavoriteFoodSellIds = favoriteRepository.findByUserAndFoodSellIsNotNull(currentUser)
                .stream()
                .map(favorite -> favorite.getFoodSell().getId())
                .toList();

        // Map pure FoodRecipeEntities to FoodRecipeCardResponse using ModelMapper
        List<FoodRecipeCardResponse> foodRecipeResponses = pureFoodRecipes.stream()
                .map(recipe -> {
                    FoodRecipeCardResponse response = modelMapper.map(recipe, FoodRecipeCardResponse.class);
                    response.setIsFavorite(userFavoriteRecipeIds.contains(recipe.getId()));

                    // Map photos from FoodRecipeEntity to structured list
                    List<PhotoDTO> photoDTOs = recipe.getPhotos().stream()
                            .map(photo -> new PhotoDTO(photo.getId(), photo.getPhoto()))
                            .collect(Collectors.toList());
                    response.setPhoto(photoDTOs);

                    return response;
                })
                .collect(Collectors.toList());


        // Map FoodSellEntities to FoodSellCardResponse using ModelMapper
        List<FoodSellCardResponse> foodSellResponses = foodSells.stream()
                .map(sell -> {
                    FoodSellCardResponse response = modelMapper.map(sell, FoodSellCardResponse.class);
                    response.setIsFavorite(userFavoriteFoodSellIds.contains(sell.getId()));

                    // Set additional fields from the related FoodRecipeEntity
                    FoodRecipeEntity linkedRecipe = sell.getFoodRecipe();

                    // Map photos from FoodRecipeEntity to structured list
                    List<PhotoDTO> photoDTOs = linkedRecipe.getPhotos().stream()
                            .map(photo -> new PhotoDTO(photo.getId(), photo.getPhoto()))
                            .collect(Collectors.toList());
                    response.setPhoto(photoDTOs);

                    response.setName(linkedRecipe.getName());
                    response.setAverageRating(linkedRecipe.getAverageRating());
                    response.setTotalRaters(linkedRecipe.getTotalRaters());

                    return response;
                })
                .collect(Collectors.toList());

        // Prepare the response map
        Map<String, List<?>> responseMap = new HashMap<>();
        responseMap.put("foodRecipes", foodRecipeResponses);
        responseMap.put("foodSells", foodSellResponses);

        // Build and return the BaseResponse directly from the service
        return BaseResponse.builder()
                .message("All foods fetched successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .payload(responseMap)
                .build();
    }


    @Override
    public BaseResponse<?> getPopularFoods() {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("User authenticated: {}", currentUser.getEmail());

        // Fetch all food recipes sorted by average rating in descending order
        List<FoodRecipeEntity> allRecipes = foodRecipeRepository.findAllByOrderByAverageRatingDesc();
        List<Long> userFavoriteRecipeIds = favoriteRepository.findByUserAndFoodRecipeIsNotNull(currentUser)
                .stream()
                .map(favorite -> favorite.getFoodRecipe().getId())
                .toList();

        // Filter out food recipes that are linked to food sells
        List<FoodRecipeEntity> pureFoodRecipes = allRecipes.stream()
                .filter(recipe -> foodSellRepository.findByFoodRecipe(recipe).isEmpty())
                .collect(Collectors.toList());

        // Fetch all food sells sorted by average rating in descending order
        List<FoodSellEntity> popularSells = foodSellRepository.findAllByOrderByAverageRatingDesc();
        List<Long> userFavoriteFoodSellIds = favoriteRepository.findByUserAndFoodSellIsNotNull(currentUser)
                .stream()
                .map(favorite -> favorite.getFoodSell().getId())
                .toList();

        // Map pure FoodRecipeEntities to FoodRecipeCardResponse using ModelMapper
        List<FoodRecipeCardResponse> popularRecipeResponses = pureFoodRecipes.stream()
                .map(recipe -> {
                    FoodRecipeCardResponse response = modelMapper.map(recipe, FoodRecipeCardResponse.class);
                    response.setIsFavorite(userFavoriteRecipeIds.contains(recipe.getId()));

                    // Map photos from FoodRecipeEntity to structured list
                    List<PhotoDTO> photoDTOs = recipe.getPhotos().stream()
                            .map(photo -> new PhotoDTO(photo.getId(), photo.getPhoto()))
                            .collect(Collectors.toList());
                    response.setPhoto(photoDTOs);

                    return response;
                })
                .collect(Collectors.toList());

        // Map FoodSellEntities to FoodSellCardResponse using ModelMapper
        List<FoodSellCardResponse> popularSellResponses = popularSells.stream()
                .map(sell -> {
                    FoodSellCardResponse response = modelMapper.map(sell, FoodSellCardResponse.class);
                    response.setIsFavorite(userFavoriteFoodSellIds.contains(sell.getId()));

                    // Set additional fields from the related FoodRecipeEntity
                    FoodRecipeEntity linkedRecipe = sell.getFoodRecipe();

                    // Map photos from FoodRecipeEntity to structured list
                    List<PhotoDTO> photoDTOs = linkedRecipe.getPhotos().stream()
                            .map(photo -> new PhotoDTO(photo.getId(), photo.getPhoto()))
                            .collect(Collectors.toList());
                    response.setPhoto(photoDTOs);

                    response.setName(linkedRecipe.getName());
                    response.setAverageRating(linkedRecipe.getAverageRating());
                    response.setTotalRaters(linkedRecipe.getTotalRaters());

                    return response;
                })
                .collect(Collectors.toList());

        // Prepare the response map
        Map<String, List<?>> responseMap = new HashMap<>();
        responseMap.put("popularRecipes", popularRecipeResponses);
        responseMap.put("popularSells", popularSellResponses);

        // Build and return the BaseResponse directly from the service
        return BaseResponse.builder()
                .message("Popular foods fetched successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .payload(responseMap)
                .build();
    }


}

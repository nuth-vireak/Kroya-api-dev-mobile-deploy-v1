package com.kshrd.kroya_api.service.User;

import com.kshrd.kroya_api.entity.FavoriteEntity;
import com.kshrd.kroya_api.entity.UserEntity;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.FoodRecipe.FoodRecipeCardResponse;
import com.kshrd.kroya_api.repository.Favorite.FavoriteRepository;
import com.kshrd.kroya_api.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final ModelMapper modelMapper;

    @Override
    public BaseResponse<?> getAllFavoriteFoodRecipesByCurrentUser() {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Fetching favorite food recipes for user: {}", currentUser.getEmail());

        // Fetch all favorite food recipes for the user
        List<FavoriteEntity> favoriteEntities = favoriteRepository.findByUserAndFoodRecipeIsNotNull(currentUser);

        // Use ModelMapper to map the favorite entities to FoodRecipeCardResponse
        List<FoodRecipeCardResponse> favoriteFoodRecipes = favoriteEntities.stream()
                .map(favorite -> {
                    // Use ModelMapper to convert FoodRecipeEntity to FoodRecipeCardResponse
                    FoodRecipeCardResponse response = modelMapper.map(favorite.getFoodRecipe(), FoodRecipeCardResponse.class);
                    response.setIsFavorite(true);  // Mark it as favorite
                    return response;
                })
                .collect(Collectors.toList());

        // Build the BaseResponse here in the service layer
        return BaseResponse.builder()
                .message("Favorite food recipes fetched successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .payload(favoriteFoodRecipes)
                .build();
    }
}

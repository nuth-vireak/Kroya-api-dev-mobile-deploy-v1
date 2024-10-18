package com.kshrd.kroya_api.service.FoodSell;

import com.kshrd.kroya_api.dto.FoodRecipeDTO;
import com.kshrd.kroya_api.dto.PhotoDTO;
import com.kshrd.kroya_api.dto.UserDTO;
import com.kshrd.kroya_api.entity.*;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.FoodSell.FoodSellCardResponse;
import com.kshrd.kroya_api.payload.FoodSell.FoodSellRequest;
import com.kshrd.kroya_api.payload.FoodSell.FoodSellResponse;
import com.kshrd.kroya_api.repository.Favorite.FavoriteRepository;
import com.kshrd.kroya_api.repository.FoodRecipe.FoodRecipeRepository;
import com.kshrd.kroya_api.repository.FoodSell.FoodSellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodSellServiceImpl implements FoodSellService {

    private final FoodRecipeRepository foodRecipeRepository;
    private final FoodSellRepository foodSellRepository;
    private final FavoriteRepository favoriteRepository;
    private final ModelMapper modelMapper;

    @Override
    public BaseResponse<?> createFoodSell(FoodSellRequest foodSellRequest, Long foodRecipeId) {

        // Fetch the FoodRecipeEntity by ID
        Optional<FoodRecipeEntity> foodRecipeOptional = foodRecipeRepository.findById(foodRecipeId.intValue());
        log.info("Food Recipe ID: {}", foodRecipeId);

        if (foodRecipeOptional.isEmpty()) {
            log.error("Food Recipe with ID {} not found", foodRecipeId);
            return BaseResponse.builder()
                    .message("Food Recipe not found")
                    .statusCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
                    .build();
        }

        FoodRecipeEntity foodRecipeEntity = foodRecipeOptional.get();

        // Create a new FoodSellEntity
        FoodSellEntity foodSellEntity = FoodSellEntity.builder()
                .foodRecipe(foodRecipeEntity)
                .dateCooking(foodSellRequest.getDateCooking())
                .amount(foodSellRequest.getAmount())
                .price(foodSellRequest.getPrice())
                .location(foodSellRequest.getLocation())
                .status(foodSellRequest.getStatus())
                .build();

        // Save the FoodSellEntity to the database
        FoodSellEntity savedFoodSell = foodSellRepository.save(foodSellEntity);
        log.info("FoodSell entity saved successfully with ID: {}", savedFoodSell.getId());

        // Map the photos from FoodRecipeEntity to PhotoDTO
        List<PhotoDTO> photoDTOs = foodRecipeEntity.getPhotos().stream()
                .map(photoEntity -> new PhotoDTO(photoEntity.getId(), photoEntity.getPhoto()))
                .toList();

        // Map FoodRecipeEntity to FoodRecipeDTO including photos
        FoodRecipeDTO foodRecipeDTO = modelMapper.map(foodRecipeEntity, FoodRecipeDTO.class);
        foodRecipeDTO.setPhoto(photoDTOs);

        // Check if the saved food sell is a favorite for the current user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isFavorite = favoriteRepository.existsByUserAndFoodSell(currentUser, savedFoodSell);

        // Map FoodSellEntity to FoodSellResponse and set the mapped FoodRecipeDTO
        FoodSellResponse foodSellResponse = modelMapper.map(savedFoodSell, FoodSellResponse.class);
        foodSellResponse.setFoodRecipeDTO(foodRecipeDTO);
        foodSellResponse.setIsFavorite(isFavorite);

//        // Map FoodSellEntity to FoodSellResponse and set the mapped FoodRecipeDTO
//        FoodSellResponse foodSellResponse = modelMapper.map(savedFoodSell, FoodSellResponse.class);
//        foodSellResponse.setFoodRecipeDTO(foodRecipeDTO);

//        // Configure ModelMapper to map FoodRecipeEntity inside FoodSellEntity to FoodRecipeDTO
//        modelMapper.typeMap(FoodSellEntity.class, FoodSellResponse.class).addMappings(mapper ->
//                mapper.map(src -> src.getFoodRecipe(), FoodSellResponse::setFoodRecipeDTO)
//        );
//
//        // Use ModelMapper to map FoodSellEntity to FoodSellResponse
//        FoodSellResponse foodSellResponse = modelMapper.map(savedFoodSell, FoodSellResponse.class);

        return BaseResponse.builder()
                .message("FoodSell created successfully")
                .statusCode(String.valueOf(HttpStatus.CREATED.value()))
                .payload(foodSellResponse)
                .build();
    }

    @Override
    public BaseResponse<?> getAllFoodSells() {

        // Get the currently authenticated user
        UserEntity currentUser = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("User authenticated: {}", currentUser.getEmail());

        // Fetch all FoodSellEntity records from the database
        List<FoodSellEntity> foodSellEntities = foodSellRepository.findAll();

        // Fetch the user's favorite sell items
        List<FavoriteEntity> userFavorites = favoriteRepository.findByUserAndFoodSellIsNotNull(currentUser);
        List<Long> userFavoriteSellIds = userFavorites.stream()
                .map(favorite -> favorite.getFoodSell().getId())
                .toList();

        // Map each FoodSellEntity to FoodSellCardResponse using ModelMapper
        List<FoodSellCardResponse> foodSellCardResponses = foodSellEntities.stream()
                .map(foodSellEntity -> {
                    // Map using ModelMapper
                    FoodSellCardResponse response = modelMapper.map(foodSellEntity, FoodSellCardResponse.class);

                    // Set additional fields from the related FoodRecipeEntity
                    FoodRecipeEntity linkedRecipe = foodSellEntity.getFoodRecipe();

                    // Map photos from FoodRecipeEntity to structured list
                    List<PhotoDTO> photoDTOs = linkedRecipe.getPhotos().stream()
                            .map(photo -> new PhotoDTO(photo.getId(), photo.getPhoto()))
                            .collect(Collectors.toList());
                    response.setPhoto(photoDTOs);

                    response.setName(linkedRecipe.getName());
                    response.setAverageRating(linkedRecipe.getAverageRating());
                    response.setTotalRaters(linkedRecipe.getTotalRaters());

                    // Set isFavorite based on user preferences
                    response.setIsFavorite(userFavoriteSellIds.contains(foodSellEntity.getId()));

                    return response;
                })
                .collect(Collectors.toList());

        // Return the response with the list of FoodSellCardResponse objects
        return BaseResponse.builder()
                .message("All FoodSell records fetched successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .payload(foodSellCardResponses)
                .build();
    }

}

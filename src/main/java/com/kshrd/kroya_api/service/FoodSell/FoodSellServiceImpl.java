package com.kshrd.kroya_api.service.FoodSell;

import com.kshrd.kroya_api.dto.FoodRecipeDTO;
import com.kshrd.kroya_api.dto.UserDTO;
import com.kshrd.kroya_api.entity.FoodRecipeEntity;
import com.kshrd.kroya_api.entity.FoodSellEntity;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.FoodSell.FoodSellRequest;
import com.kshrd.kroya_api.payload.FoodSell.FoodSellResponse;
import com.kshrd.kroya_api.repository.FoodRecipe.FoodRecipeRepository;
import com.kshrd.kroya_api.repository.FoodSell.FoodSellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodSellServiceImpl implements FoodSellService {

    private final FoodRecipeRepository foodRecipeRepository;
    private final FoodSellRepository foodSellRepository;

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

        // Map UserEntity to UserDTO
        UserDTO userDTO = new UserDTO(
                foodRecipeEntity.getUser().getUserId(),
                foodRecipeEntity.getUser().getFullName(),
                foodRecipeEntity.getUser().getProfileImage()
        );

        // Map FoodRecipeEntity to FoodRecipeDTO
        FoodRecipeDTO foodRecipeDTO = new FoodRecipeDTO(
                foodRecipeEntity.getId(),
                foodRecipeEntity.getPhotoUrl(),
                foodRecipeEntity.getName(),
                foodRecipeEntity.getDescription(),
                foodRecipeEntity.getDurationInMinutes(),
                foodRecipeEntity.getLevel(),
                foodRecipeEntity.getIngredients(),
                foodRecipeEntity.getCookingSteps(),
                foodRecipeEntity.getIsForSale(),
                foodRecipeEntity.getCreatedAt(),
                userDTO  // Minimal user info
        );

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

        // Build FoodSellResponse with FoodRecipeDTO and UserDTO
        FoodSellResponse foodSellResponse = new FoodSellResponse(
                savedFoodSell.getId(),
                foodRecipeDTO,  // Use FoodRecipeDTO
                savedFoodSell.getDateCooking(),
                savedFoodSell.getAmount(),
                savedFoodSell.getPrice(),
                savedFoodSell.getLocation(),
                savedFoodSell.getStatus()
        );

        return BaseResponse.builder()
                .message("FoodSell created successfully")
                .statusCode(String.valueOf(HttpStatus.CREATED.value()))
                .payload(foodSellResponse)
                .build();
    }
}

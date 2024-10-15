package com.kshrd.kroya_api.service.FoodSell;

import com.kshrd.kroya_api.entity.FoodRecipeEntity;
import com.kshrd.kroya_api.entity.FoodSellEntity;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.FoodSell.FoodSellCardResponse;
import com.kshrd.kroya_api.payload.FoodSell.FoodSellRequest;
import com.kshrd.kroya_api.payload.FoodSell.FoodSellResponse;
import com.kshrd.kroya_api.repository.FoodRecipe.FoodRecipeRepository;
import com.kshrd.kroya_api.repository.FoodSell.FoodSellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
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

        // Configure ModelMapper to map FoodRecipeEntity inside FoodSellEntity to FoodRecipeDTO
        modelMapper.typeMap(FoodSellEntity.class, FoodSellResponse.class).addMappings(mapper ->
                mapper.map(src -> src.getFoodRecipe(), FoodSellResponse::setFoodRecipeDTO)
        );

        // Use ModelMapper to map FoodSellEntity to FoodSellResponse
        FoodSellResponse foodSellResponse = modelMapper.map(savedFoodSell, FoodSellResponse.class);

        return BaseResponse.builder()
                .message("FoodSell created successfully")
                .statusCode(String.valueOf(HttpStatus.CREATED.value()))
                .payload(foodSellResponse)
                .build();
    }

    @Override
    public BaseResponse<?> getAllFoodSells() {
        // Fetch all FoodSellEntity records from the database
        List<FoodSellEntity> foodSellEntities = foodSellRepository.findAll();

        // Map each FoodSellEntity to a FoodSellCardResponse
        List<FoodSellCardResponse> foodSellCardResponses = foodSellEntities.stream().map(foodSellEntity -> {
            // Get the relevant fields from FoodSellEntity and map them to FoodSellCardResponse
            return new FoodSellCardResponse(
                    foodSellEntity.getId(),                         // ID from FoodSellEntity
                    foodSellEntity.getFoodRecipe().getPhotoUrl(),  // Photo URL from FoodRecipeEntity
                    foodSellEntity.getFoodRecipe().getName(),      // Name from FoodRecipeEntity
                    foodSellEntity.getDateCooking(),               // Cooking date from FoodSellEntity
                    foodSellEntity.getPrice(),                     // Price from FoodSellEntity
                    foodSellEntity.getFoodRecipe().getAverageRating(),  // Average rating from FoodRecipeEntity
                    foodSellEntity.getFoodRecipe().getTotalRaters()     // Total raters from FoodRecipeEntity
            );
        }).collect(Collectors.toList());

        // Return the list of FoodSellCardResponse in a BaseResponse
        return BaseResponse.builder()
                .message("All FoodSell records fetched successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .payload(foodSellCardResponses)
                .build();
    }
}

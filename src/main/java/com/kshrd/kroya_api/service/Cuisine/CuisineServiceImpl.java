package com.kshrd.kroya_api.service.Cuisine;

import com.kshrd.kroya_api.entity.CuisineEntity;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.Cuisine.CuisineRequest;
import com.kshrd.kroya_api.repository.Cuisine.CuisineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CuisineServiceImpl implements CuisineService {

    private final CuisineRepository cuisineRepository;

    @Override
    public BaseResponse<?> postCuisine(CuisineRequest cuisineRequest) {

        log.info("Received request to create a cuisine with name: {}", cuisineRequest.getCuisineName());

        // Creating a new CategoryEntity object from the request
        CuisineEntity cuisineEntity = new CuisineEntity();
        cuisineEntity.setCuisineName(cuisineRequest.getCuisineName());

        // Saving the new category to the database
        cuisineRepository.save(cuisineEntity);
        log.info("Cuisine saved successfully with ID: {}", cuisineEntity.getId());

        // Building a successful response
        return BaseResponse.builder()
                .payload(cuisineEntity)
                .message("Cuisine created successfully")
                .statusCode(String.valueOf(HttpStatus.CREATED.value()))
                .build();
    }

    @Override
    public BaseResponse<?> getAllCuisine() {

        log.info("Fetching all cuisines...");
        List<CuisineEntity> cuisines = cuisineRepository.findAllByOrderById();

        if (cuisines.isEmpty()) {
            log.info("No cuisines found.");
            return BaseResponse.builder()
                    .message("No cuisines found")
                    .statusCode(String.valueOf(HttpStatus.NO_CONTENT.value()))
                    .build();
        }

        log.info("cuisines fetched successfully, count: {}", cuisines.size());
        return BaseResponse.builder()
                .payload(cuisines)
                .message("Categories fetched successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .build();
    }
}

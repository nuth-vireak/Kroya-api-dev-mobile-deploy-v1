package com.kshrd.kroya_api.service.Category;

import com.kshrd.kroya_api.entity.CategoryEntity;
import com.kshrd.kroya_api.entity.FoodRecipeEntity;
import com.kshrd.kroya_api.entity.FoodSellEntity;
import com.kshrd.kroya_api.entity.UserEntity;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.Category.CategoryRequest;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImp implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public BaseResponse<?> postCategory(CategoryRequest categoryRequest) {
        log.info("Received request to create a category with name: {}", categoryRequest.getCategoryName());

        // Creating a new CategoryEntity object from the request
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryName(categoryRequest.getCategoryName());

        // Saving the new category to the database
        categoryRepository.save(categoryEntity);
        log.info("Category saved successfully with ID: {}", categoryEntity.getId());

        // Building a successful response
        return BaseResponse.builder()
                .payload(categoryEntity)
                .message("Category created successfully")
                .statusCode(String.valueOf(HttpStatus.CREATED.value()))
                .build();
    }

    @Override
    public BaseResponse<?> getAllCategory() {
        log.info("Fetching all categories...");
        List<CategoryEntity> categories = categoryRepository.findAllByOrderById();

        if (categories.isEmpty()) {
            log.info("No categories found.");
            return BaseResponse.builder()
                    .message("No categories found")
                    .statusCode(String.valueOf(HttpStatus.NO_CONTENT.value()))
                    .build();
        }

        log.info("Categories fetched successfully, count: {}", categories.size());
        return BaseResponse.builder()
                .payload(categories)
                .message("Categories fetched successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .build();
    }
}

package com.kshrd.kroya_api.controller;

import com.kshrd.kroya_api.enums.ItemType;
import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.service.Favorite.FavoriteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/favorite")
@AllArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/add-favorite")
    public BaseResponse<?> saveFoodToFavorite(
            @RequestParam Long foodId,
            @RequestParam ItemType itemType
    ) {
        return favoriteService.saveFoodToFavorite(foodId, itemType);
    }

    @DeleteMapping("/remove-favorite")
    public BaseResponse<?> removeFoodFromFavorite(
            @RequestParam Long foodId,
            @RequestParam ItemType itemType
    ) {
        return favoriteService.unsavedFoodFromFavorite(foodId, itemType);
    }

    @GetMapping("/all")
    public BaseResponse<?> getAllFavoriteFoodsByCurrentUser() {
        return favoriteService.getAllFavoriteFoodsByCurrentUser();
    }
}

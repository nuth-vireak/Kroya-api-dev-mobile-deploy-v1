package com.kshrd.kroya_api.controller;

import com.kshrd.kroya_api.payload.BaseResponse;
import com.kshrd.kroya_api.payload.Cuisine.CuisineRequest;
import com.kshrd.kroya_api.service.Cuisine.CuisineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/cuisine")
@RequiredArgsConstructor
@Slf4j
public class CuisineController {

    private final CuisineService cuisineService;

    @PostMapping("/post-cuisine")
    public BaseResponse<?> postCategory(@RequestBody CuisineRequest cuisineRequest){
        return cuisineService.postCuisine(cuisineRequest);
    }

    @GetMapping("/all")
    public BaseResponse<?> getAllCuisine(){
        return cuisineService.getAllCuisine();
    }

}

package com.kshrd.kroya_api.service.User;

import com.kshrd.kroya_api.payload.BaseResponse;

public interface UserService {
    BaseResponse<?> getAllFavoriteFoodRecipesByCurrentUser();
}

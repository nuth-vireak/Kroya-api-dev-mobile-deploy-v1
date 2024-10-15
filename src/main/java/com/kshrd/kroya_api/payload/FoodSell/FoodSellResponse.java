package com.kshrd.kroya_api.payload.FoodSell;

import com.kshrd.kroya_api.dto.FoodRecipeDTO;
import com.kshrd.kroya_api.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodSellResponse {
    private Long id;
    private FoodRecipeDTO foodRecipeDTO;
    private Date dateCooking;
    private Integer amount;
    private Double price;
    private String location;
    private Boolean status;
    private ItemType itemType = ItemType.FOOD_SELL;
}

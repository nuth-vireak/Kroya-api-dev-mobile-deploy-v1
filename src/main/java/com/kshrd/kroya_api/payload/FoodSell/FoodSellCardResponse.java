package com.kshrd.kroya_api.payload.FoodSell;

import com.kshrd.kroya_api.dto.PhotoDTO;
import com.kshrd.kroya_api.entity.PhotoEntity;
import com.kshrd.kroya_api.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodSellCardResponse {
    private Long id;
    private List<PhotoDTO> photo;
    private String name;
    private Date dateCooking;
    private Double price;
    private Double averageRating;
    private Integer totalRaters;
    private Boolean isFavorite;
    private ItemType itemType = ItemType.FOOD_SELL;
}

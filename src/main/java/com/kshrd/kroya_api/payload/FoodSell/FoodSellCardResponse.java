package com.kshrd.kroya_api.payload.FoodSell;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodSellCardResponse {
    private Long id;
    private String photoUrl;
    private String name;
    private Date dateCooking;
    private Double price;
    private Double averageRating;
    private Integer totalRaters;
}

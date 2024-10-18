package com.kshrd.kroya_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kshrd.kroya_api.enums.ItemType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "food_sell_tb")
public class FoodSellEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(name = "date_cooking")
    private Date dateCooking;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "price")
    private Double price;

    @Column(name = "location")
    private String location;

    @Column(name = "status")
    private Boolean status;

    @OneToOne
    @JoinColumn(name = "food_recipe_id")
    private FoodRecipeEntity foodRecipe;
}

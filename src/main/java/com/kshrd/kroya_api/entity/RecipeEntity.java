package com.kshrd.kroya_api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kshrd.kroya_api.payload.Recipe.Ingredient;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "recipe_tb")
public class RecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_in_minutes")
    private Integer durationInMinutes;

    @Column(name = "level", length = 50)
    private String level;

    @Column(name = "cuisine", length = 50)
    private String cuisine;

    @Column(name = "category", length = 50)
    private String category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ingredients", columnDefinition = "jsonb")
    private List<Ingredient> ingredients;

    @Column(name = "is_for_sale")
    private Boolean isForSale;

    @Column(name = "cooking_steps")
    private String cookingSteps;

    @Column(name = "total_raters")
    private Integer totalRaters;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default now()")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserEntity user;
}

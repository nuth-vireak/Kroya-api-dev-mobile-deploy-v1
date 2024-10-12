package com.kshrd.kroya_api.repository.Recipe;

import com.kshrd.kroya_api.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<RecipeEntity, Integer> {
}

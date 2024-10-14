package com.kshrd.kroya_api.repository.Category;

import com.kshrd.kroya_api.entity.CategoryEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity,Long> {
CategoryEntity findByCategoryName(String categoryName, PageRequest pageRequest);
CategoryEntity getByCategoryName(String categoryName);
List<CategoryEntity> findAllByOrderById();



}

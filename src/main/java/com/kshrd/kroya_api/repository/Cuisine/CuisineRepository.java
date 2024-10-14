package com.kshrd.kroya_api.repository.Cuisine;

import com.kshrd.kroya_api.entity.CuisineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuisineRepository extends JpaRepository<CuisineEntity, Long> {
    List<CuisineEntity> findAllByOrderById();
}

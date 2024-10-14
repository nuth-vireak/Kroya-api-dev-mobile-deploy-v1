package com.kshrd.kroya_api.repository.FoodSell;

import com.kshrd.kroya_api.entity.FoodSellEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodSellRepository extends JpaRepository<FoodSellEntity, Long> {
}

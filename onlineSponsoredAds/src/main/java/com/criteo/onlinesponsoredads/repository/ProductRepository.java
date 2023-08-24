package com.criteo.onlinesponsoredads.repository;

import com.criteo.onlinesponsoredads.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> getAllBySerialNumberIn(List<String> serialNums);

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.campaigns c " +
            "WHERE :currentDate <= c.endDate " +
            "AND :currentDate >= c.startDate " +
            "AND p.category = :category " +
            "ORDER BY p.price DESC ")
    List<Product> findProductsWithHighestBidInActiveCampaign(String category, Instant currentDate);

    Product findFirstByOrderByPriceDesc();

}

package com.criteo.onlinesponsoredads.service;

import com.criteo.onlinesponsoredads.domain.Product;
import com.criteo.onlinesponsoredads.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getAllProductsBySerialNumbers(List<String> serialNums) {
        return repository.getAllBySerialNumberIn(serialNums);
    }

    public Product getProductWithHighestBidByCategory(String category, Instant currDate){
        List<Product> prodList = repository.findProductsWithHighestBidInActiveCampaign(category, currDate, PageRequest.of(0,1));
        return prodList.stream().findFirst().orElse(null);
    }
}

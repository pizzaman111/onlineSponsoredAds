package com.criteo.onlinesponsoredads.domain.dto;

import com.criteo.onlinesponsoredads.domain.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter
@NoArgsConstructor
public class ProductDto {

    private Long id;
    private String title;
    private String category;
    private double price;
    private String serialNumber;

    public ProductDto(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.category = product.getCategory();
        this.price = product.getPrice();
        this.serialNumber = product.getSerialNumber();
    }

}

package com.criteo.onlinesponsoredads;

import com.criteo.onlinesponsoredads.domain.Product;
import com.criteo.onlinesponsoredads.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    ProductRepository productRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        List<Product> products = List.of(new Product("iPhone", "electronics", 300.0, "aaaa"),
                                        new Product("galaxy tablet", "electronics", 600.0, "bbbb"),
                                        new Product("chair", "furniture", 50.0, "cccc"),
                                        new Product("table", "furniture", 150.0, "dddd"),
                                        new Product("wheelbarrow", "gardening", 250.0, "eeee"),
                                        new Product("shovel", "gardening", 30.0, "ffff"),
                                        new Product("frying pan", "cooking", 80.0, "gggg"),
                                        new Product("silverware", "cooking", 180.0, "hhhh"),
                                        new Product("diamond ring", "jewellery", 2000.0, "pppp"),
                                        new Product("wall clock", "decorative", 120.0, "rrrr"));

        productRepository.saveAllAndFlush(products);

    }
}

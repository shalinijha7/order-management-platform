package com.orderplatform.inventory.config;

import com.orderplatform.inventory.entity.Product;
import com.orderplatform.inventory.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Seeds a few demo products on startup so you can test order flow immediately.
@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            productRepository.save(new Product(null, "Mechanical Keyboard", 2999.0, 50));
            productRepository.save(new Product(null, "Wireless Mouse", 799.0, 100));
            productRepository.save(new Product(null, "27-inch Monitor", 15999.0, 20));
        }
    }
}

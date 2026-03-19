package com.example.userservice.service;

import com.example.userservice.messaging.MessagePublisher;
import com.example.userservice.model.EmailNotification;
import com.example.userservice.model.Product;
import com.example.userservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    ProductRepository productRepository;
    private final MessagePublisher messagePublisher;

    public ProductService(ProductRepository productRepository, MessagePublisher messagePublisher) {
        this.productRepository = productRepository;
        this.messagePublisher = messagePublisher;
    }

    @Transactional
    public Product createProduct(Product product) {
        // Check if code already exists
        if (productRepository.existsByCode(product.getCode())) {
            throw new IllegalArgumentException("Code already in use: " + product.getCode());
        }

        Product savedProduct = productRepository.save(product);
        logger.info("Product created successfully: {}", savedProduct.getId());

        try {

            EmailNotification notification = EmailNotification.forNewProductRegistration(savedProduct);

            messagePublisher.publishEmailNotification(notification);
        } catch (Exception e) {
            logger.error("Failed to process email notification: {}", e.getMessage());
        }

        return savedProduct;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}

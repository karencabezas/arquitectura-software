package com.demo.productmanagement.service;

import com.demo.productmanagement.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    /**
     * Retrieves all products
     * @return List of all products
     */
    List<Product> getAllProducts();

    /**
     * Retrieves a product by its ID
     * @param id The product ID
     * @return Optional containing the product if found, empty otherwise
     */
    Optional<Product> getProductById(Long id);

    /**
     * Saves a new product
     * @param product The product to save
     * @return The saved product with generated ID
     */
    Product saveProduct(Product product);

    /**
     * Updates an existing product
     * @param product The product to update
     * @return true if updated successfully, false otherwise
     */
    boolean updateProduct(Product product);

    /**
     * Deletes a product by its ID
     * @param id The product ID to delete
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteProduct(Long id);
}
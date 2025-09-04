package com.vibe.pay.backend.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public Product createProduct(Product product) {
        productMapper.insert(product);
        return product;
    }

    public Optional<Product> getProductById(Long id) {
        return Optional.ofNullable(productMapper.findById(id));
    }

    public List<Product> getAllProducts() {
        return productMapper.findAll();
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product existingProduct = productMapper.findById(id);
        if (existingProduct == null) {
            throw new RuntimeException("Product not found with id " + id);
        }
        productDetails.setId(id); // Ensure the ID is set for update
        productMapper.update(productDetails);
        return productDetails;
    }

    public void deleteProduct(Long id) {
        productMapper.delete(id);
    }
}
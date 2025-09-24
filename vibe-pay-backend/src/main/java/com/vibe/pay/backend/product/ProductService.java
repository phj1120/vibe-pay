package com.vibe.pay.backend.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public Product createProduct(Product product) {
        productMapper.insert(product);
        return product;
    }

    public Optional<Product> getProductById(Long productId) {
        return Optional.ofNullable(productMapper.findByProductId(productId));
    }

    public List<Product> getAllProducts() {
        return productMapper.findAll();
    }

    public Product updateProduct(Long productId, Product productDetails) {
        Product existingProduct = productMapper.findByProductId(productId);
        if (existingProduct == null) {
            throw new IllegalArgumentException("Product not found with id " + productId);
        }
        productDetails.setProductId(productId); // Ensure the ID is set for update
        productMapper.update(productDetails);
        return productDetails;
    }

    public void deleteProduct(Long productId) {
        productMapper.delete(productId);
    }
}
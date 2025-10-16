package com.vibe.pay.domain.product.service;

import com.vibe.pay.domain.product.dto.ProductRequest;
import com.vibe.pay.domain.product.entity.Product;
import com.vibe.pay.domain.product.repository.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 상품 서비스
 * 상품 관리 비즈니스 로직을 처리하는 계층
 *
 * @see Product
 * @see ProductMapper
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    /**
     * 상품 생성
     *
     * @param request 상품 생성 요청 DTO
     * @return 생성된 상품 엔티티
     * @throws RuntimeException 상품 생성 실패 시
     */
    @Transactional
    public Product createProduct(ProductRequest request) {
        log.info("Creating new product: name={}", request.getName());

        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());

        productMapper.insert(product);
        log.info("Product created successfully: productId={}", product.getProductId());

        return product;
    }

    /**
     * 상품 ID로 상품 조회
     *
     * @param productId 상품 ID
     * @return 조회된 상품 엔티티 (Optional)
     */
    public Optional<Product> getProductById(Long productId) {
        log.debug("Fetching product by ID: productId={}", productId);
        return productMapper.findByProductId(productId);
    }

    /**
     * 전체 상품 목록 조회
     *
     * @return 상품 엔티티 목록
     */
    public List<Product> getAllProducts() {
        log.debug("Fetching all products");
        return productMapper.findAll();
    }

    /**
     * 상품 정보 수정
     *
     * @param productId 수정할 상품 ID
     * @param request 상품 수정 요청 DTO
     * @return 수정된 상품 엔티티
     * @throws RuntimeException 상품을 찾을 수 없거나 수정 실패 시
     */
    @Transactional
    public Product updateProduct(Long productId, ProductRequest request) {
        log.info("Updating product: productId={}", productId);

        Product product = productMapper.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        product.setName(request.getName());
        product.setPrice(request.getPrice());

        productMapper.update(product);
        log.info("Product updated successfully: productId={}", productId);

        return product;
    }

    /**
     * 상품 삭제
     *
     * @param productId 삭제할 상품 ID
     * @throws RuntimeException 상품을 찾을 수 없거나 삭제 실패 시
     */
    @Transactional
    public void deleteProduct(Long productId) {
        log.info("Deleting product: productId={}", productId);

        Product product = productMapper.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        productMapper.delete(productId);
        log.info("Product deleted successfully: productId={}", productId);
    }
}

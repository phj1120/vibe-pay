package com.vibe.pay.backend.product;

import com.vibe.pay.backend.exception.ProductInUseException;
import com.vibe.pay.backend.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper productMapper;

    /**
     * 상품 생성
     *
     * @param request 상품 생성 요청
     * @return 생성된 상품
     * @throws IllegalArgumentException 상품명이 비어있거나 가격이 0 이하일 경우
     */
    @Transactional
    public Product createProduct(ProductRequest request) {
        log.debug("상품 생성 시작: {}", request.getName());

        // 유효성 검증
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다");
        }

        // Product 엔티티 생성
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .build();

        // DB에 저장
        productMapper.insert(product);

        log.info("상품 생성 완료: id={}, name={}", product.getProductId(), product.getName());
        return product;
    }

    /**
     * 상품 ID로 조회
     *
     * @param productId 상품 ID
     * @return 상품 정보 (Optional)
     */
    public Optional<Product> getProductById(Long productId) {
        log.debug("상품 조회: productId={}", productId);
        Product product = productMapper.selectById(productId);
        return Optional.ofNullable(product);
    }

    /**
     * 전체 상품 목록 조회
     *
     * @return 상품 목록
     */
    public List<Product> getAllProducts() {
        log.debug("전체 상품 목록 조회");
        return productMapper.selectAll();
    }

    /**
     * 상품 정보 수정
     *
     * @param productId 상품 ID
     * @param request   수정 요청
     * @return 수정된 상품
     * @throws ProductNotFoundException 상품이 존재하지 않을 경우
     * @throws IllegalArgumentException 가격이 0 이하일 경우
     */
    @Transactional
    public Product updateProduct(Long productId, ProductRequest request) {
        log.debug("상품 수정 시작: productId={}", productId);

        // 상품 존재 여부 확인
        Product existingProduct = productMapper.selectById(productId);
        if (existingProduct == null) {
            throw new ProductNotFoundException("상품을 찾을 수 없습니다: " + productId);
        }

        // 가격 유효성 검증
        if (request.getPrice() != null && request.getPrice() <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다");
        }

        // Product 엔티티 생성
        Product product = Product.builder()
                .productId(productId)
                .name(request.getName() != null ? request.getName() : existingProduct.getName())
                .price(request.getPrice() != null ? request.getPrice() : existingProduct.getPrice())
                .build();

        // DB 업데이트
        productMapper.update(product);

        log.info("상품 수정 완료: id={}, name={}", product.getProductId(), product.getName());
        return product;
    }

    /**
     * 상품 삭제
     *
     * @param productId 상품 ID
     * @throws ProductNotFoundException 상품이 존재하지 않을 경우
     * @throws ProductInUseException    상품이 주문에서 사용 중일 경우
     */
    @Transactional
    public void deleteProduct(Long productId) {
        log.debug("상품 삭제 시작: productId={}", productId);

        // 상품 존재 여부 확인
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new ProductNotFoundException("상품을 찾을 수 없습니다: " + productId);
        }

        // TODO: Phase 2-5에서 주문 내역 체크 구현 예정
        // 현재는 무조건 삭제 가능

        // DB에서 삭제
        productMapper.delete(productId);

        log.info("상품 삭제 완료: id={}", productId);
    }
}

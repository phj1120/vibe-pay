package com.vibe.pay.backend.product;

import com.vibe.pay.backend.exception.ProductInUseException;
import com.vibe.pay.backend.exception.ProductNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@RestController
@RequestMapping("/api/products")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "상품 관리", description = "상품 CRUD API")
public class ProductController {
    private final ProductService productService;

    /**
     * 전체 상품 목록 조회
     *
     * @return 상품 목록
     */
    @Operation(summary = "상품 목록 조회", description = "전체 상품 목록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.debug("GET /api/products - 상품 목록 조회");

        List<Product> products = productService.getAllProducts();
        List<ProductResponse> response = products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * 상품 ID로 조회
     *
     * @param id 상품 ID
     * @return 상품 정보
     */
    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상세 정보를 조회합니다")
    @Parameter(name = "id", description = "상품 ID", required = true, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.debug("GET /api/products/{} - 상품 상세 조회", id);

        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(toResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 상품 생성
     *
     * @param request 상품 생성 요청
     * @return 생성된 상품
     */
    @Operation(summary = "상품 생성", description = "새로운 상품을 생성합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        log.debug("POST /api/products - 상품 생성: {}", request.getName());

        try {
            Product product = productService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(product));
        } catch (IllegalArgumentException e) {
            log.warn("상품 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 상품 수정
     *
     * @param id      상품 ID
     * @param request 수정 요청
     * @return 수정된 상품
     */
    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다")
    @Parameter(name = "id", description = "상품 ID", required = true, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        log.debug("PUT /api/products/{} - 상품 수정", id);

        try {
            Product product = productService.updateProduct(id, request);
            return ResponseEntity.ok(toResponse(product));
        } catch (ProductNotFoundException e) {
            log.warn("상품 수정 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("상품 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 상품 삭제
     *
     * @param id 상품 ID
     * @return 삭제 결과
     */
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다")
    @Parameter(name = "id", description = "상품 ID", required = true, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "상품이 사용 중이어서 삭제 불가")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.debug("DELETE /api/products/{} - 상품 삭제", id);

        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (ProductNotFoundException e) {
            log.warn("상품 삭제 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (ProductInUseException e) {
            log.warn("상품 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Product를 ProductResponse로 변환
     *
     * @param product Product 엔티티
     * @return ProductResponse DTO
     */
    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        return response;
    }
}

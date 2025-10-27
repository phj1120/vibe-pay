package com.vibe.pay.backend.product;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Mapper
public interface ProductMapper {
    void insert(Product product);

    Product selectById(Long productId);

    List<Product> selectAll();

    void update(Product product);

    void delete(Long productId);
}

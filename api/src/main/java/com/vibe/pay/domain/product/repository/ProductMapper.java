package com.vibe.pay.domain.product.repository;

import com.vibe.pay.domain.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ProductMapper {
    void insert(Product product);
    Optional<Product> findByProductId(Long productId);
    List<Product> findAll();
    void update(Product product);
    void delete(Long productId);
}

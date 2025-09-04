package com.vibe.pay.backend.product;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ProductMapper {
    List<Product> findAll();
    Product findById(Long id);
    void insert(Product product);
    void update(Product product);
    void delete(Long id);
}
package com.zebrands.products.repository;

import com.zebrands.products.dto.ProductDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRedisRepository extends CrudRepository<ProductDto, Integer> {
}

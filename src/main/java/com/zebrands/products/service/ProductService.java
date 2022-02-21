package com.zebrands.products.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zebrands.products.dto.ProductDto;

import java.util.List;

public interface ProductService {

    ProductDto save(ProductDto productDto);

    List<ProductDto> findAll(String user) throws JsonProcessingException;

    ProductDto update(int sku, ProductDto productDto, String user) throws JsonProcessingException;

    Boolean delete(int sku);
}

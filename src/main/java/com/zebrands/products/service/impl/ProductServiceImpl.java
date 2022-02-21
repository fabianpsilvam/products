package com.zebrands.products.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zebrands.products.constants.Constants;
import com.zebrands.products.dto.NotificationDto;
import com.zebrands.products.dto.ProductDto;
import com.zebrands.products.entity.ProductEntity;
import com.zebrands.products.manager.KafkaProducerManager;
import com.zebrands.products.repository.ProductRedisRepository;
import com.zebrands.products.repository.ProductRepository;
import com.zebrands.products.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductRedisRepository productRedisRepository;

    @Autowired
    private KafkaProducerManager kafkaProducerManager;

    @Autowired
    private ObjectMapper objectMapper;

    public ProductDto save(ProductDto productDto) {
        var newProduct = productRepository.save(ProductEntity.builder()
                .name(productDto.getName())
                .price(productDto.getPrice())
                .brand(productDto.getBrand())
                .build());
        productDto.setSku(newProduct.getSku());
        productRedisRepository.save(productDto);
        return productDto;
    }

    @Override
    public List<ProductDto> findAll(String user) throws JsonProcessingException {

        List<ProductDto> products = new ArrayList<>();
        productRedisRepository.findAll().forEach(products::add);
        if (products.isEmpty()) {
            products = productRepository.findAll().stream().map(productEntity ->
                            new ProductDto(
                                    productEntity.getSku(),
                                    productEntity.getName(),
                                    productEntity.getPrice(),
                                    productEntity.getBrand()))
                    .collect(Collectors.toList());
        }
        kafkaProducerManager.sendMessage(objectMapper.writeValueAsString(NotificationDto.builder()
                .type(Constants.TRACKING)
                .user(user)
                .params(products)
                .build()));
        return products;
    }

    public ProductDto update(int sku, ProductDto productDto, String user) throws JsonProcessingException {
        var productFound = findById(sku);
        productFound.setName(productDto.getName());
        productFound.setPrice(productDto.getPrice());
        productFound.setBrand(productDto.getBrand());

        productRepository.save(productFound);
        var productEdited = new ProductDto(productFound.getSku(), productFound.getName(), productFound.getPrice(), productFound.getBrand());
        productRedisRepository.save(productEdited);
        kafkaProducerManager.sendMessage(objectMapper.writeValueAsString(NotificationDto.builder()
                .type(Constants.EMAIL)
                .user(user)
                .params(productEdited)
                .build()));
        return productEdited;
    }

    public Boolean delete(int sku) {
        var productFound = findById(sku);
        productRepository.delete(productFound);
        productRedisRepository.deleteById(productFound.getSku());
        return Boolean.TRUE;
    }

    private ProductEntity findById(int sku) {
        return productRepository.findById(sku)
                .orElseThrow(() -> new InvalidParameterException("Product not found on: " + sku));
    }
}

package com.zebrands.products.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zebrands.products.annotations.Authorization;
import com.zebrands.products.constants.Constants;
import com.zebrands.products.dto.ProductDto;
import com.zebrands.products.service.ProductService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = "/product")
@Log4j2
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping()
    @Authorization(userType = {Constants.USER, Constants.ADMIN})
    public ResponseEntity<List<ProductDto>> findAllProducts(HttpServletRequest httpServletRequest) throws JsonProcessingException {
        var user = httpServletRequest.getHeader(Constants.AUTH_USER);
        log.info("find all user: {}", user);
        return ResponseEntity.ok(productService.findAll(user));
    }

    @PostMapping()
    @Authorization(userType = {Constants.ADMIN})
    public ResponseEntity<ProductDto> saveProduct(@RequestBody ProductDto productDto, HttpServletRequest httpServletRequest) {
        var user = httpServletRequest.getHeader(Constants.AUTH_USER);
        log.info("save product user: {} product: {}", user, productDto.toString());
        return ResponseEntity.ok(productService.save(productDto));
    }

    @PutMapping("/{sku}")
    @Authorization(userType = {Constants.ADMIN})
    public ResponseEntity<ProductDto> updateProduct(@PathVariable(value = "sku") int sku, @RequestBody ProductDto productDto, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        var user = httpServletRequest.getHeader(Constants.AUTH_USER);
        log.info("update product user: {} product: {}", user, productDto.toString());
        return ResponseEntity.ok(productService.update(sku, productDto, user));
    }

    @DeleteMapping("/{sku}")
    @Authorization(userType = {Constants.ADMIN})
    public ResponseEntity<Boolean> deleteProduct(@PathVariable(value = "sku") int sku, HttpServletRequest httpServletRequest) {
        var user = httpServletRequest.getHeader(Constants.AUTH_USER);
        log.info("delete product user: {} sku: {}", user, sku);
        return ResponseEntity.ok(productService.delete(sku));
    }
}

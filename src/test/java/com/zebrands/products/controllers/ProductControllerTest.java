package com.zebrands.products.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zebrands.products.CommonTests;
import com.zebrands.products.constants.Constants;
import com.zebrands.products.dto.ProductDto;
import com.zebrands.products.entity.ProductEntity;
import com.zebrands.products.requesters.AuthorizationRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductControllerTest extends CommonTests {

    @BeforeEach
    public void setup() {
        productRedisRepository.deleteAll();
    }

    @MockBean
    private AuthorizationRequest authorizationRequest;

    @Test
    public void findAllProducts_IsEmpty() throws Exception {
        Mockito.when(authorizationRequest.getAuthorizationByUser(Mockito.anyString())).thenReturn(Constants.USER);

        var request = MockMvcRequestBuilders
                .get("/product")
                .header("AUTH_USER", "1234");

        var response = mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        var productsResponse = objectMapper.readValue(response, new TypeReference<List<ProductDto>>() {
        });

        Assertions.assertEquals(productsResponse.size(), 0);
    }

    @Test
    @Sql(scripts = {"/db/query/add_products.sql"})
    public void findAllProducts_User() throws Exception {
        Mockito.when(authorizationRequest.getAuthorizationByUser(Mockito.anyString())).thenReturn(Constants.USER);

        var request = MockMvcRequestBuilders
                .get("/product")
                .header("AUTH_USER", "1234");

        var response = mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        var productsResponse = objectMapper.readValue(response, new TypeReference<List<ProductDto>>() {
        });
        productsResponse = productsResponse.stream().map(p -> new ProductDto(0, p.getName(), p.getPrice(), p.getBrand())).collect(Collectors.toList());
        var productExcepted = new ArrayList<ProductDto>();
        productExcepted.add(new ProductDto(0, "shoes", 10f, "Nike"));
        productExcepted.add(new ProductDto(0, "t-shirt", 11f, "Lee"));

        Mockito.verify(kafkaProducerManager, Mockito.times(1)).sendMessage(Mockito.anyString());
        Assertions.assertEquals(productsResponse, productExcepted);
    }

    @Test
    @Sql(scripts = {"/db/query/add_products.sql"})
    public void findAllProducts_Admin() throws Exception {
        Mockito.when(authorizationRequest.getAuthorizationByUser(Mockito.anyString())).thenReturn(Constants.ADMIN);

        var request = MockMvcRequestBuilders
                .get("/product")
                .header("AUTH_USER", "1234");

        var response = mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        var productsResponse = objectMapper.readValue(response, new TypeReference<List<ProductDto>>() {
        });
        productsResponse = productsResponse.stream().map(p -> new ProductDto(0, p.getName(), p.getPrice(), p.getBrand())).collect(Collectors.toList());
        var productExcepted = new ArrayList<ProductDto>();
        productExcepted.add(new ProductDto(0, "shoes", 10f, "Nike"));
        productExcepted.add(new ProductDto(0, "t-shirt", 11f, "Lee"));

        Mockito.verify(kafkaProducerManager, Mockito.times(1)).sendMessage(Mockito.anyString());
        Assertions.assertEquals(productsResponse, productExcepted);
    }

    @Test
    @Sql(scripts = {"/db/query/add_products.sql"})
    public void findAllProducts_Unauthorized() throws Exception {
        Mockito.when(authorizationRequest.getAuthorizationByUser(Mockito.anyString())).thenReturn("NO_TYPE");

        var request = MockMvcRequestBuilders
                .get("/product")
                .header("AUTH_USER", "1234");

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void saveProduct_Admin() throws Exception {
        Mockito.when(authorizationRequest.getAuthorizationByUser(Mockito.anyString())).thenReturn(Constants.ADMIN);

        var product = ProductDto.builder().name("shoes").brand("nike").price(10f).build();
        var request = MockMvcRequestBuilders
                .post("/product")
                .content(objectMapper.writeValueAsString(product))
                .contentType(MediaType.APPLICATION_JSON)
                .header("AUTH_USER", "1234");

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        var products = productRepository.findAll();

        List<ProductDto> productsRedis = new ArrayList<>();
        productRedisRepository.findAll().forEach(productsRedis::add);

        Assertions.assertEquals(productsRedis.size(), 1);
        Assertions.assertEquals(products.size(), 1);
        Assertions.assertEquals(products.get(0).getBrand(), product.getBrand());
        Assertions.assertEquals(products.get(0).getName(), product.getName());
        Assertions.assertEquals(products.get(0).getPrice(), product.getPrice());
    }

    @Test
    public void saveProduct_Unauthorized() throws Exception {
        Mockito.when(authorizationRequest.getAuthorizationByUser(Mockito.anyString())).thenReturn(Constants.USER);

        var product = ProductDto.builder().name("shoes").brand("nike").price(10f).build();
        var request = MockMvcRequestBuilders
                .post("/product")
                .content(objectMapper.writeValueAsString(product))
                .contentType(MediaType.APPLICATION_JSON)
                .header("AUTH_USER", "1234");

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void updateProduct_Admin() throws Exception {
        Mockito.when(authorizationRequest.getAuthorizationByUser(Mockito.anyString())).thenReturn(Constants.ADMIN);

        var product = ProductEntity.builder().name("shoes").brand("nike").price(10f).build();
        var newProduct = productRepository.save(product);

        var productDto = ProductDto.builder().name("T-shirt").brand("Lee").price(11f).build();
        var request = MockMvcRequestBuilders
                .put("/product/" + newProduct.getSku())
                .content(objectMapper.writeValueAsString(productDto))
                .contentType(MediaType.APPLICATION_JSON)
                .header("AUTH_USER", "1234");

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        var products = productRepository.findAll();
        List<ProductDto> productsRedis = new ArrayList<>();
        productRedisRepository.findAll().forEach(productsRedis::add);

        Assertions.assertEquals(productsRedis.size(), 1);
        Assertions.assertEquals(products.size(), 1);
        Assertions.assertEquals(products.get(0).getBrand(), productDto.getBrand());
        Assertions.assertEquals(products.get(0).getName(), productDto.getName());
        Assertions.assertEquals(products.get(0).getPrice(), productDto.getPrice());
        Mockito.verify(kafkaProducerManager, Mockito.times(1)).sendMessage(Mockito.anyString());
    }

    @Test
    public void updateProduct_Unauthorized() throws Exception {
        Mockito.when(authorizationRequest.getAuthorizationByUser(Mockito.anyString())).thenReturn(Constants.USER);

        var product = ProductEntity.builder().name("shoes").brand("nike").price(10f).build();
        var newProduct = productRepository.save(product);

        var productDto = ProductDto.builder().name("T-shirt").brand("Lee").price(11f).build();
        var request = MockMvcRequestBuilders
                .put("/product/" + newProduct.getSku())
                .content(objectMapper.writeValueAsString(productDto))
                .contentType(MediaType.APPLICATION_JSON)
                .header("AUTH_USER", "1234");

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void updateProduct_NotExistProduct() throws Exception {
        Mockito.when(authorizationRequest.getAuthorizationByUser(Mockito.anyString())).thenReturn(Constants.ADMIN);

        var productDto = ProductDto.builder().name("T-shirt").brand("Lee").price(11f).build();
        var request = MockMvcRequestBuilders
                .put("/product/1")
                .content(objectMapper.writeValueAsString(productDto))
                .contentType(MediaType.APPLICATION_JSON)
                .header("AUTH_USER", "1234");

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void deleteProduct_Admin() throws Exception {
        Mockito.when(authorizationRequest.getAuthorizationByUser(Mockito.anyString())).thenReturn(Constants.ADMIN);

        var product = ProductEntity.builder().name("shoes").brand("nike").price(10f).build();
        var newProduct = productRepository.save(product);

        var request = MockMvcRequestBuilders
                .delete("/product/" + newProduct.getSku())
                .header("AUTH_USER", "1234");

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        var products = productRepository.findAll();
        List<ProductDto> productsRedis = new ArrayList<>();
        productRedisRepository.findAll().forEach(productsRedis::add);

        Assertions.assertTrue(productsRedis.isEmpty());
        Assertions.assertEquals(products.size(), 0);
    }

    @Test
    public void deleteProduct_Unauthorized() throws Exception {
        Mockito.when(authorizationRequest.getAuthorizationByUser(Mockito.anyString())).thenReturn(Constants.USER);

        var product = ProductEntity.builder().name("shoes").brand("nike").price(10f).build();
        var newProduct = productRepository.save(product);

        var request = MockMvcRequestBuilders
                .delete("/product/" + newProduct.getSku())
                .header("AUTH_USER", "1234");

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void deleteProduct_NotExistProduct() throws Exception {
        Mockito.when(authorizationRequest.getAuthorizationByUser(Mockito.anyString())).thenReturn(Constants.ADMIN);

        var request = MockMvcRequestBuilders
                .delete("/product/1")
                .header("AUTH_USER", "1234");

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}

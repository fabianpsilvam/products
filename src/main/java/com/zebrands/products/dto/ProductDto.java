package com.zebrands.products.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@RedisHash("Product")
public class ProductDto implements Serializable {

    @Id
    private int sku;

    private String name;

    private Float price;

    private String brand;

    @Override
    public String toString() {
        return "sku: " + sku +
                "name: " + name +
                "price: " + price +
                "brand: " + brand;
    }
}

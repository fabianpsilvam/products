package com.zebrands.products.entity;

import lombok.*;

import javax.persistence.*;

@Entity(name = "Product")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int sku;

    @Column
    private String name;

    @Column
    private Float price;

    @Column
    private String brand;
}

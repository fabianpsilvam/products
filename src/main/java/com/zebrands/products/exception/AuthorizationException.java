package com.zebrands.products.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class AuthorizationException extends RuntimeException {

    private String code;
    private String message;
}

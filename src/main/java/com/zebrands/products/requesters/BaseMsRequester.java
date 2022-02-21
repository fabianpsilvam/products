package com.zebrands.products.requesters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BaseMsRequester {

    @Value("${microservices.base_url}")
    private String microServiceBasePath;

    public String getBasePath() {
        return microServiceBasePath;
    }
}

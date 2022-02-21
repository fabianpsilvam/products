package com.zebrands.products.requesters.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zebrands.products.constants.Constants;
import com.zebrands.products.constants.Routes;
import com.zebrands.products.requesters.AuthorizationRequest;
import com.zebrands.products.requesters.BaseMsRequester;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Log4j2
public class AuthorizationRequestImpl extends BaseMsRequester implements AuthorizationRequest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String getAuthorizationByUser(String user) throws JsonProcessingException {
        ResponseEntity<String> response;
        var url = getBasePath() + Routes.PROFILE_BY_USER + user;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class);
        } catch (Exception e) {
            log.error(e);
            response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (response.getStatusCode().is4xxClientError()) {
            log.error("Url {},  Error 4xx response {},  with request",
                    url, objectMapper.writeValueAsString(response.getBody()));
        }

        if (response.getStatusCode().is5xxServerError()) {
            log.error("Url {},  Error 5xx response {},  with request",
                    url, objectMapper.writeValueAsString(response.getBody()));
        }

        return response.getBody();
    }
}

package com.zebrands.products.requesters;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface AuthorizationRequest {

    String getAuthorizationByUser(String user) throws JsonProcessingException;
}

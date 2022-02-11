package com.tier3.tier3graphql.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class LoginRequest {

    private String username;
    private String password;

}

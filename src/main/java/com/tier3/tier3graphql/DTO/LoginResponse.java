package com.tier3.tier3graphql.DTO;

import com.tier3.tier3graphql.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class LoginResponse {

    private String token;
    private User user;

}

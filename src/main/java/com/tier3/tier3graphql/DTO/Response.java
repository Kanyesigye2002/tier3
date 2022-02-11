package com.tier3.tier3graphql.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response {

    private boolean status;
    private String message;

}

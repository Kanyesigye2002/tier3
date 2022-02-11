package com.tier3.tier3graphql.DTO;

import lombok.Data;

@Data
public class UpdatePassword {

    private String oldPassword;
    private String newPassword;

}

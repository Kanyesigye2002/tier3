package com.tier3.tier3graphql.model.util;

import com.tier3.tier3graphql.model.User;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String token;

    @ManyToOne(cascade = CascadeType.ALL)
    private User user;

}

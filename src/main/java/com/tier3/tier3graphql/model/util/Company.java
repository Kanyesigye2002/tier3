package com.tier3.tier3graphql.model.util;

import com.tier3.tier3graphql.model.HomePage;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String urlImage;

}

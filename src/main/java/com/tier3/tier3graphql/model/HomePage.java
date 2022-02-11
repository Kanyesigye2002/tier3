package com.tier3.tier3graphql.model;

import com.tier3.tier3graphql.model.util.Achievement;
import com.tier3.tier3graphql.model.util.Company;
import com.tier3.tier3graphql.model.util.FeaturesCategory;
import com.tier3.tier3graphql.model.util.Review;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Data
@Entity
public class HomePage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<Company> companies = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<Achievement> achievement = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<FeaturesCategory> featuresCategories = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<Review> reviews = new ArrayList<>();

}

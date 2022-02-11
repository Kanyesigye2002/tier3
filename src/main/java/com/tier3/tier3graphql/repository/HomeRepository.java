package com.tier3.tier3graphql.repository;

import com.tier3.tier3graphql.model.HomePage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeRepository extends CrudRepository<HomePage, Long> {

}

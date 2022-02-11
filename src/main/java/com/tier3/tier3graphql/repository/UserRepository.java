package com.tier3.tier3graphql.repository;

import com.tier3.tier3graphql.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String userName);

    boolean existsByUsername(String userName);

}

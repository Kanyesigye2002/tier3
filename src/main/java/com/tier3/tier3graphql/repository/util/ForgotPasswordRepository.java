package com.tier3.tier3graphql.repository.util;

import com.tier3.tier3graphql.model.util.ForgotPassword;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForgotPasswordRepository extends CrudRepository<ForgotPassword, Long> {

    ForgotPassword findByToken(String token);

}

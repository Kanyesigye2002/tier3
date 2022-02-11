package com.tier3.tier3graphql.repository.util;

import com.tier3.tier3graphql.model.util.EmailVerification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationRepository extends CrudRepository<EmailVerification, Long> {

    EmailVerification findByToken(String token);

}

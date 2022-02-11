package com.tier3.tier3graphql.repository.util;

import com.tier3.tier3graphql.model.util.NewPassword;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewPasswordRepository extends CrudRepository<NewPassword, Long> {

    NewPassword findByToken(String token);

}

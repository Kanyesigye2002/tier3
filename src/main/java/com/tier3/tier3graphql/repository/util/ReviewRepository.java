package com.tier3.tier3graphql.repository.util;

import com.tier3.tier3graphql.model.util.Review;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends CrudRepository<Review, Long> {

}

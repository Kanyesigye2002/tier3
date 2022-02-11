package com.tier3.tier3graphql.repository.util;

import com.tier3.tier3graphql.model.util.Project;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Long> {

}

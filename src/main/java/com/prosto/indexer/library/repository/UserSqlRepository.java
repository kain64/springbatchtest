package com.prosto.indexer.library.repository;

import com.prosto.indexer.library.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserSqlRepository extends CrudRepository<User, String> {
}

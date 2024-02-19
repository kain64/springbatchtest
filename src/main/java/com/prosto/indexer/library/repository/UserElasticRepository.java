package com.prosto.indexer.library.repository;

import com.prosto.indexer.library.entity.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserElasticRepository extends ElasticsearchRepository<User, String> {
}

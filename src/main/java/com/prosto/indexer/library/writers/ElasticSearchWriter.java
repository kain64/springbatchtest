package com.prosto.indexer.library.writers;

import com.prosto.indexer.library.entity.User;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.prosto.indexer.library.repository.UserElasticRepository;

@Component
public class ElasticSearchWriter  implements ItemWriter<User> {
    private final UserElasticRepository elasticRepository;
    @Autowired
    public ElasticSearchWriter(UserElasticRepository elasticRepository) {
        this.elasticRepository = elasticRepository;
    }

    @Override
    public void write(Chunk<? extends User> chunk) throws Exception {
        elasticRepository.saveAll(chunk);
    }
}

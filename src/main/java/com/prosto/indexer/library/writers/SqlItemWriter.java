package com.prosto.indexer.library.writers;

import com.prosto.indexer.library.entity.User;
import com.prosto.indexer.library.repository.UserSqlRepository;
import jakarta.transaction.Transactional;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SqlItemWriter implements ItemWriter<User> {

    private final UserSqlRepository userSqlRepository;
    @Autowired
    public SqlItemWriter(UserSqlRepository userSqlRepository){

        this.userSqlRepository = userSqlRepository;
    }
    @Transactional
    @Override
    public void write(Chunk<? extends User> chunk) throws Exception {
        userSqlRepository.saveAll(chunk);
    }
}

package com.prosto.indexer.library.processors;

import com.prosto.indexer.library.entity.User;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class UsersElasticItemProcessor implements ItemProcessor<User, User> {

    @Override
    public User process(User item) {
        item.setFirstname(null);
        item.setLastname(null);
        return item;
    }
}

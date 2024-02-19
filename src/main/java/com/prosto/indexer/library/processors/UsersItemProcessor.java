package com.prosto.indexer.library.processors;

import com.prosto.indexer.library.entity.User;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UsersItemProcessor implements ItemProcessor<User, User> {

    @Override
    public User process(User item) {
        item.setId(UUID.randomUUID().toString());
        item.setFullname(item.getFirstname()+" " + item.getLastname());
        return item;
    }
}

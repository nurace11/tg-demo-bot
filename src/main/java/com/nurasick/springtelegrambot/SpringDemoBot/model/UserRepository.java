package com.nurasick.springtelegrambot.SpringDemoBot.model;

import com.nurasick.springtelegrambot.SpringDemoBot.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}

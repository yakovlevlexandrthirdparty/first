package ru.skb.rentguy.first.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.skb.rentguy.first.entities.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findByTelegramId(long telegramId);
}

package ru.skb.rentguy.first.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.skb.rentguy.first.entities.Advert;
import ru.skb.rentguy.first.entities.Role;

@Repository
public interface AdvertRepository extends CrudRepository<Advert, Long> {
    Advert findAllByAuthorId(String authorId);
}

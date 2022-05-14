package ru.skb.rentguy.first.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.skb.rentguy.first.entities.Order;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
}

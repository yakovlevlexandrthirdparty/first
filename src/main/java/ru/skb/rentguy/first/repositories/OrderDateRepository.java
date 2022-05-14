package ru.skb.rentguy.first.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.skb.rentguy.first.entities.OrderDate;

import java.util.List;

@Repository
public interface OrderDateRepository extends CrudRepository<OrderDate, Long> {
    List<OrderDate> findAllByRecipientId(Long recipientId);

    List<OrderDate> findAllByAdvertId(Long advertId);
}

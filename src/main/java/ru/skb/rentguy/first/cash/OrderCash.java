package ru.skb.rentguy.first.cash;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import ru.skb.rentguy.first.entities.Advert;
import ru.skb.rentguy.first.entities.Order;

import java.util.HashMap;
import java.util.Map;

@Service
@Setter
@Getter
public class OrderCash {
    private final Map<Long, Order> orderMap = new HashMap<>();

    public void saveOrder(long userId, Order order) {
        orderMap.put(userId, order);
    }
}

package ru.skb.rentguy.first.cash;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import ru.skb.rentguy.first.entities.Advert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Setter
@Getter
public class AdvertCash {
    private final Map<Long, Advert> advertMap = new HashMap<>();

    public void saveAdvert(long userId, Advert advert) {
        advertMap.put(userId,advert);
    }
}

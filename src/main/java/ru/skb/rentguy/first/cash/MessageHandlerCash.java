package ru.skb.rentguy.first.cash;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import ru.skb.rentguy.first.model.BotState;

import java.util.HashMap;
import java.util.Map;

@Service
@Setter
@Getter
public class MessageHandlerCash {
    private final Map<Long, BotState> botStateMap = new HashMap<>();

    public void saveBotCategoryState(long userId, BotState botState) {
            botStateMap.put(userId, botState);
        }
}

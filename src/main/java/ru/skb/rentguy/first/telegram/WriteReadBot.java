package ru.skb.rentguy.first.telegram;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import ru.skb.rentguy.first.cash.BotStateCash;
import ru.skb.rentguy.first.cash.MessageHandlerCash;
import ru.skb.rentguy.first.constants.bot.BotMessageEnum;
import ru.skb.rentguy.first.entities.User;
import ru.skb.rentguy.first.model.BotState;
import ru.skb.rentguy.first.repositories.UserRepository;
import ru.skb.rentguy.first.telegram.handlers.CallbackQueryHandler;
import ru.skb.rentguy.first.telegram.handlers.MessageHandler;

import java.util.Date;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WriteReadBot extends SpringWebhookBot {
    private String botPath;
    private String botUsername;
    private String botToken;

    @Autowired
    private BotStateCash botStateCash;

    @Autowired
    private MessageHandlerCash messageHandlerCash;

    @Autowired
    UserRepository userRepository;

    private MessageHandler messageHandler;
    private CallbackQueryHandler callbackQueryHandler;

    public WriteReadBot(SetWebhook setWebhook, MessageHandler messageHandler, CallbackQueryHandler callbackQueryHandler) {
        super(setWebhook);
        this.messageHandler = messageHandler;
        this.callbackQueryHandler = callbackQueryHandler;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        try {
            return handleUpdate(update);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return SendMessage.builder()
                    .chatId(update.hasMessage()?String.valueOf(update.getMessage().getChatId()):String.valueOf(update.getCallbackQuery().getFrom().getId()))
                    .text(BotMessageEnum.EXCEPTION_ILLEGAL_MESSAGE.getMessage()+" "+e.getMessage())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return SendMessage.builder()
                    .chatId(update.hasMessage()?String.valueOf(update.getMessage().getChatId()):String.valueOf(update.getCallbackQuery().getFrom().getId()))
                    .text(BotMessageEnum.EXCEPTION_WHAT_THE_FUCK.getMessage())
                    .build();
        }
    }

    private BotApiMethod<?> handleUpdate(Update update) {
        if (update.hasMessage()) {
            long userId = update.getMessage().getFrom().getId();
            if (userRepository.findByTelegramId(userId) == null) {
                System.out.println("NEW USER MS");
                User user = new User();
                user.setFirstName(update.getMessage().getFrom().getFirstName());
                user.setLastName(update.getMessage().getFrom().getLastName());
                user.setUserName(update.getMessage().getFrom().getUserName());
                user.setTelegramId(userId);
                User u = userRepository.save(user);
                System.out.println("user id:" + u.getId() + " " + u.getTelegramId());
            }
            return handleInputMessage(update.getMessage());
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            long userId = update.getCallbackQuery().getFrom().getId();
            if (userRepository.findByTelegramId(userId) == null) {
                System.out.println("NEW USER CQ");
                User user = new User();
                user.setFirstName(update.getCallbackQuery().getFrom().getFirstName());
                user.setLastName(update.getCallbackQuery().getFrom().getLastName());
                user.setUserName(update.getCallbackQuery().getFrom().getUserName());
                user.setTelegramId(userId);
                User u = userRepository.save(user);
                System.out.println("user id:" + u.getId() + " " + u.getTelegramId());
            }
            return callbackQueryHandler.processCallbackQuery(callbackQuery);
        }
        return null;
    }

    private BotApiMethod<?> handleInputMessage(Message message) {
        BotState botState;
        BotState msgHandlerState;
        String inputMsg = message.getText();

        switch (inputMsg) {
            case "/start":
                botState = BotState.START;
                msgHandlerState = BotState.START;
                break;
            default:
                botState = botStateCash.getBotStateMap().get(message.getFrom().getId()) == null ?
                        BotState.START : botStateCash.getBotStateMap().get(message.getFrom().getId());
                msgHandlerState = messageHandlerCash.getBotStateMap().get(message.getFrom().getId()) == null ?
                        BotState.START : messageHandlerCash.getBotStateMap().get(message.getFrom().getId());
        }
        return messageHandler.handle(message, botState, msgHandlerState);

    }
}
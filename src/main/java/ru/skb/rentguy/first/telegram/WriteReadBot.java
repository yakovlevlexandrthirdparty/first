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
import ru.skb.rentguy.first.model.BotState;
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
            return SendMessage.builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .text(BotMessageEnum.EXCEPTION_ILLEGAL_MESSAGE.getMessage() + " " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return new SendMessage(update.getMessage().getChatId().toString(),
                    BotMessageEnum.EXCEPTION_WHAT_THE_FUCK.getMessage());
        }
    }

    private BotApiMethod<?> handleUpdate(Update update) {
        if (update.hasMessage()) {
            System.out.println("User>" + update.getMessage().getFrom().toString());
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            return callbackQueryHandler.processCallbackQuery(callbackQuery);
        } else if (update.hasMessage()) {
            return handleInputMessage(update.getMessage());
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
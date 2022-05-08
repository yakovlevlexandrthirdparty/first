package ru.skb.rentguy.first.telegram.handlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.skb.rentguy.first.cash.BotStateCash;
import ru.skb.rentguy.first.model.BotState;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class MessageHandler {
    BotStateCash botStateCash;
    @Autowired
    CallbackQueryHandler callbackQueryHandler;

    public BotApiMethod<?> handle(Message message, BotState botState) {
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        //if new user
        //TODO: save new user to repository
//        if (!userDAO.isExist(userId)) {
//            return eventHandler.saveNewUser(message, userId, sendMessage);
//        }
        //save state in to cache
        botStateCash.saveBotState(userId, botState);
        //if state =...
        switch (botState.name()) {
            case ("START"):
                return callbackQueryHandler.getMainMenuMessage(message.getChatId(), userId);
            case ("RENT_CAR"):
            case ("RENT_APARTMENT"):
            case ("APARTMENTS_LIST"):
                return getSendMessage(String.valueOf(chatId));
            case ("INPUT_DATES"):
                return enterDates(message);
            case ("INPUT_BEDROOMS"):
                return enterBedRooms(message);
            default:
                throw new IllegalStateException("Unexpected value: " + botState);
        }
    }

    public BotApiMethod<?> enterDates(Message message) {
        long userId = message.getFrom().getId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String stringStartDate = message.getText().substring(0,message.getText().indexOf("-"));
        String stringEndDate = message.getText().substring(message.getText().indexOf("-")+1);
        System.out.println("START_DATE:" + stringStartDate);
        System.out.println("END_DATE:" + stringEndDate);
        try {
            Date startDate = simpleDateFormat.parse(stringStartDate);
            Date endDate = simpleDateFormat.parse(stringEndDate);
            sendMessage.setText("Дата заезда: " + new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(startDate) +
                    "\nДата выезда: "+ new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(endDate));
        } catch (ParseException e) {
            sendMessage.setText("Exception");
            e.printStackTrace();
        }
        sendMessage.setReplyMarkup(getInlineMessageButtons(List.of("Да, даты верны|acceptDates")));
        botStateCash.saveBotState(userId, BotState.INPUT_BEDROOMS);
        return sendMessage;
    }

    public BotApiMethod<?> enterBedRooms(Message message) {
        long userId = message.getFrom().getId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText("Подтвердите количество комнат: " + message.getText());
        sendMessage.setReplyMarkup(getInlineMessageButtons(List.of("Да, количество комнат верно|acceptBedRooms")));
        botStateCash.saveBotState(userId, BotState.APARTMENTS_LIST);
        return sendMessage;
    }

    public InlineKeyboardMarkup getInlineMessageButtons(List<String> btnList) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        btnList.stream().forEach( btnString -> {
            InlineKeyboardButton inputDates = new InlineKeyboardButton();
            inputDates.setText(btnString.substring(0,btnString.indexOf("|")));
            inputDates.setCallbackData(btnString.substring(btnString.indexOf("|")+1));

            List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
            keyboardButtonsRow1.add(inputDates);
            rowList.add(keyboardButtonsRow1);

        });
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private SendMessage getSendMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(chatId, "Используйте пункты меню.");
        return sendMessage;
    }

}
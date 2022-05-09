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
import ru.skb.rentguy.first.cash.MessageHandlerCash;
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
    @Autowired
    BotStateCash botStateCash;
    @Autowired
    MessageHandlerCash messageHandlerCash;

    @Autowired
    CallbackQueryHandler callbackQueryHandler;

    public BotApiMethod<?> handle(Message message, BotState botState, BotState msgHandlerStage) {
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();
        System.out.println("STATE>>"+botState.name()+"|"+msgHandlerStage.name()+"|"+message.getText());
        //if new user
        //TODO: save new user to repository
//        if (!userDAO.isExist(userId)) {
//            return eventHandler.saveNewUser(message, userId, sendMessage);
//        }
        //save state in to cache
        botStateCash.saveBotState(userId, botState);
        messageHandlerCash.saveBotCategoryState(userId, msgHandlerStage);
        //if state =...
        switch (msgHandlerStage.name()) {
            case "START":
                return callbackQueryHandler.getMainMenuMessage(message.getChatId(), userId, new InlineKeyboardMarkup());
            case "CAR":
            case "APARTMENT":
            case "INPUT_DATES":
                return enterDates(message, msgHandlerStage);
            case "INPUT_BEDROOMS":
                return enterBedRooms(message);
            case "INPUT_PRICES":
                return enterPrices(message);
            case "APARTMENTS_LIST":
            case "CAR_LIST":
                return getSendMessage(String.valueOf(chatId));
            default:
                throw new IllegalStateException("Unexpected value: " + botState);
        }
    }

    public BotApiMethod<?> enterDates(Message message, BotState msgHandlerStage) {
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
            sendMessage.setText("Дата начала аренды: " + new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(startDate) +
                    "\nДата окончания аренды: "+ new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(endDate));
        } catch (ParseException e) {
            sendMessage.setText("Exception");
            e.printStackTrace();
        }
        sendMessage.setReplyMarkup(getInlineMessageButtons(List.of("Да, даты верны|acceptDates")));
        if(msgHandlerStage.equals(BotState.APARTMENT)) {
            messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_BEDROOMS);
        } else {
            messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_PRICES);
        }
        return sendMessage;
    }

    public BotApiMethod<?> enterBedRooms(Message message) {
        long userId = message.getFrom().getId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText("Подтвердите количество комнат: " + message.getText());
        sendMessage.setReplyMarkup(getInlineMessageButtons(List.of("Да, количество комнат верно|acceptBedRooms")));
        messageHandlerCash.saveBotCategoryState(userId,BotState.APARTMENTS_LIST);
        return sendMessage;
    }

    public BotApiMethod<?> enterPrices(Message message) {
        long userId = message.getFrom().getId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText("Подтвердите диапазон цен: от " + message.getText());
        sendMessage.setReplyMarkup(getInlineMessageButtons(List.of("Да, диапазон цен верный|acceptPrice")));
        messageHandlerCash.saveBotCategoryState(userId,BotState.APARTMENTS_LIST);
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
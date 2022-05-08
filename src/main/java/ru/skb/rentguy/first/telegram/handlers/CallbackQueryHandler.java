package ru.skb.rentguy.first.telegram.handlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.skb.rentguy.first.cash.BotStateCash;
import ru.skb.rentguy.first.model.BotState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CallbackQueryHandler {

    @Autowired
    private final BotStateCash botStateCash;

    public BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery) throws IOException {
        final long chatId = buttonQuery.getMessage().getChatId();
        final long userId = buttonQuery.getFrom().getId();

        BotApiMethod<?> callBackAnswer = null;

        String data = buttonQuery.getData();

        switch (data) {
            case ("start"):
                callBackAnswer = getMainMenuMessage(chatId,userId);
                break;
            case ("car"):
                callBackAnswer = getCallBackMenu(chatId,"Раздел аренды автомобилей", List.of("Ввести даты начала/окончания аренды|inputDates","Назад|start"));
                botStateCash.saveBotState(userId, BotState.RENT_CAR);
                break;
            case ("apartment"):
                callBackAnswer = getCallBackMenu(chatId,"Раздел аренды Квартир", List.of("Ввести даты заезда/выезда|inputDates","Назад|start"));
                botStateCash.saveBotState(userId, BotState.RENT_APARTMENT);
                break;
            case ("inputDates"):
                callBackAnswer = new SendMessage(String.valueOf(chatId), "Введите даты заезда и выезда в формате \n\nДД.ММ.ГГГГ-ДД.ММ.ГГГГ");
                botStateCash.saveBotState(userId, BotState.INPUT_DATES);
                break;
            case ("acceptDates"):
                callBackAnswer = new SendMessage(String.valueOf(chatId), "Введите количество комнат");
                botStateCash.saveBotState(userId, BotState.INPUT_BEDROOMS);
                break;
            case ("acceptBedRooms"):
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Доступные варианты");
                sendMessage.setReplyMarkup(getInlineMessageButtons(List.of(
                        "Apartment #1|Просторный апартамент\n2000руб/сут",
                        "Apartment #2|Просторный апартамент\n1800руб/сут",
                        "Apartment #3|Просторный апартамент\n2300руб/сут",
                        "Apartment #4|Просторный апартамент\n2400руб/сут",
                        "Apartment #5|Просторный апартамент\n1000руб/сут")));
                callBackAnswer = sendMessage;
                botStateCash.saveBotState(userId, BotState.APARTMENTS_LIST);
                break;
            default:
                SendMessage sendMessage1 = new SendMessage();
                sendMessage1.setChatId(String.valueOf(chatId));
                sendMessage1.setText(data);
                sendMessage1.setReplyMarkup(getInlineMessageButtons(List.of("Назад|acceptBedRooms")));
                callBackAnswer = sendMessage1;
                botStateCash.saveBotState(userId, BotState.APARTMENT_DETAILS);
                break;
        }
        return callBackAnswer;
    }

    public SendMessage getCallBackMenu(final long chatId, String header, List menuItems){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        if (header!=null) {
            sendMessage.setText(header);
        }
        sendMessage.setReplyMarkup(getInlineMessageButtons(menuItems));
        return sendMessage;
    }

    public SendMessage getMainMenuMessage(final long chatId, final long userId) {
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(String.valueOf(chatId));
        replyMessage.setText("Привет, добропожаловать в RentGuyBot, тут можно выбрать доступное жилье и забронировать время осмотра\n\n");
        replyMessage.setReplyMarkup(getInlineMessageButtons1(userId));
        return replyMessage;
    }

    public InlineKeyboardMarkup getInlineMessageButtons1(final long userId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton carBtn = new InlineKeyboardButton();
        carBtn.setText("Автомобиль");
        carBtn.setCallbackData("car");

        InlineKeyboardButton apartmentBtn = new InlineKeyboardButton();
        apartmentBtn.setText("Квартира");
        apartmentBtn.setCallbackData("apartment");

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(carBtn);
        keyboardButtonsRow1.add(apartmentBtn);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);

        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
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
}
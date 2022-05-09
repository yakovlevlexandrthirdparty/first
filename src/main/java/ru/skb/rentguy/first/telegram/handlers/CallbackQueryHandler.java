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
import ru.skb.rentguy.first.cash.MessageHandlerCash;
import ru.skb.rentguy.first.constants.bot.BotCallBackEnum;
import ru.skb.rentguy.first.model.BotState;

import java.util.ArrayList;
import java.util.List;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CallbackQueryHandler {

    @Autowired
    private final BotStateCash botStateCash;
    @Autowired
    private final MessageHandlerCash messageHandlerCash;

    public BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery){
        final long chatId = buttonQuery.getMessage().getChatId();
        final long userId = buttonQuery.getFrom().getId();

        BotApiMethod<?> callBackAnswer = null;

        String data = buttonQuery.getData();
        switch (data) {
            case "start":
                callBackAnswer = getMainMenuMessage(chatId,userId, buttonQuery.getMessage().getReplyMarkup());
                break;
            case "car":
                callBackAnswer = getCallBackMenu(chatId,"### Раздел аренды автомобилей ###\n\nСледуйте пунктам меню:", List.of("Ввести даты начала/окончания аренды|inputDates","< Назад|start"));
                botStateCash.saveBotState(userId,BotState.CAR);
                break;
            case "apartment":
                callBackAnswer = getCallBackMenu(chatId,"### Раздел аренды Квартир ###\n\nСледуйте пунктам меню:", List.of("Ввести даты заезда/выезда|inputDates","< Назад|start"));
                botStateCash.saveBotState(userId,BotState.APARTMENT);
                break;
            case "inputDates":
                callBackAnswer = new SendMessage(
                        String.valueOf(chatId),
                        (BotState.APARTMENT.name().equals(botStateCash.getBotStateMap().get(userId).name()) ? "### Раздел аренды Квартир ###":"### Раздел аренды автомобилей ###") +
                                "\n\nВведите даты начала и окончания аренды в формате \n\nДД.ММ.ГГГГ-ДД.ММ.ГГГГ");
                messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_DATES);
                break;
            case "acceptDates":
                if (BotState.APARTMENT.equals(botStateCash.getBotStateMap().get(userId))) {
                    callBackAnswer = new SendMessage(String.valueOf(chatId), "Введите количество комнат");
                    messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_BEDROOMS);
                } else {
                    callBackAnswer = new SendMessage(String.valueOf(chatId), "Введите диапазон цен \nв формате 1000-100000:");
                    messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_PRICES);
                }
                break;
            case "acceptBedRooms":
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Доступные варианты");
                sendMessage.setReplyMarkup(getInlineMessageButtons(List.of("Садовыая-Кудринская д.6: 3100Р/сут.|Просторный апартамент\n20т.р./сут",
                        "Кривоколейный пер. д.12: 2000Р/сут.|Апартамент с видом на воду\n41т.р./сут",
                        "Лужниковсктй пер. д.22: 2800Р/сут.|Апартамент с терассой\n80т.р./сут",
                        "< В начало|start")));
                callBackAnswer = sendMessage;
                messageHandlerCash.saveBotCategoryState(userId, BotState.CAR_LIST);
                break;
            case "acceptPrice":
                SendMessage sendMessage1 = new SendMessage();
                sendMessage1.setChatId(String.valueOf(chatId));
                sendMessage1.setText("Доступные варианты");
                sendMessage1.setReplyMarkup(getInlineMessageButtons(List.of(
                        "Car #1|Mercedes-Benz S63 coupe\n20т.р./сут",
                        "Car #5|Lamborghini aventador svj\n41т.р./сут",
                        "Car #7|Ferrari sf90 stradale\n80т.р./сут",
                        "< В начало|start")));
                callBackAnswer = sendMessage1;
                messageHandlerCash.saveBotCategoryState(userId, BotState.CAR_LIST);
                break;
            case "rent":
                SendMessage sendMessage3 = new SendMessage();
                sendMessage3.setChatId(String.valueOf(chatId));
                sendMessage3.setText("Ваша зявка принята.\n\n В ближайшее время с вами свяжется менеджер для подтверждения бранирования.");
                sendMessage3.setReplyMarkup(getInlineMessageButtons(List.of("< В начало|start")));
                callBackAnswer = sendMessage3;
                messageHandlerCash.saveBotCategoryState(userId, BotState.START);
                break;
            default:
                SendMessage sendMessage2 = new SendMessage();
                sendMessage2.setChatId(String.valueOf(chatId));
                sendMessage2.setText(data);
                if (BotState.APARTMENT.equals(botStateCash.getBotStateMap().get(userId))) {
                    sendMessage2.setReplyMarkup(getInlineMessageButtons(List.of("Забронировать|rent","< Назад|acceptBedRooms")));
                } else {
                    sendMessage2.setReplyMarkup(getInlineMessageButtons(List.of("Забронировать|rent","< Назад|acceptPrice")));
                }
                callBackAnswer = sendMessage2;
                break;
        }
        return callBackAnswer;
    }

    public SendMessage getCallBackMenu(final long chatId, String header, List<String> menuItems){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        if (header!=null) {
            sendMessage.setText(header);
        }
        sendMessage.setReplyMarkup(getInlineMessageButtons(menuItems));
        return sendMessage;
    }

    public SendMessage getMainMenuMessage(final long chatId, final long userId, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(String.valueOf(chatId));
        replyMessage.setText("RentGuyBot - бот по аренде автомобией и жилья. \n\nВыбирай и бранируй автомобили и жилье в лбое время.\n\nЧтобы начать выберите категорию:");
        replyMessage.setReplyMarkup(getInlineMessageButtons1(userId,inlineKeyboardMarkup));
        return replyMessage;
    }

    public InlineKeyboardMarkup getInlineMessageButtons1(final long userId, InlineKeyboardMarkup inlineKeyboardMarkup) {

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
        InlineKeyboardMarkup inlineKeyboardMarkup1 = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList2 = new ArrayList<>();
        btnList.stream().forEach( btnString -> {
            InlineKeyboardButton inputDates = new InlineKeyboardButton();
            inputDates.setText(btnString.substring(0,btnString.indexOf("|")));
            inputDates.setCallbackData(btnString.substring(btnString.indexOf("|")+1));

            List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
            keyboardButtonsRow1.add(inputDates);
            rowList2.add(keyboardButtonsRow1);
        });
        inlineKeyboardMarkup1.setKeyboard(rowList2);

        return inlineKeyboardMarkup1;
    }
}
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
    private static final String TEXT_1 = "Садовыая-Кудринская д.6: 3100Р/сут.|Просторный апартамент\n20т.р./сут";
    private static final String TEXT_2 = "Кривоколейный пер. д.12: 2000Р/сут.|Апартамент с видом на воду\n41т.р./сут";
    private static final String TEXT_3 = "Лужниковсктй пер. д.22: 2800Р/сут.|Апартамент с терассой\n80т.р./сут";
    private static final String TEXT_4 = "Mercedes-Benz S63 coupe 20т.р./сут|Mercedes-Benz S63 coupe\n20т.р./сут";
    private static final String TEXT_5 = "Lamborghini aventador svj 41т.р./сут|Lamborghini aventador svj\n41т.р./сут";
    private static final String TEXT_6 = "Ferrari sf90 stradale 80т.р./сут|Ferrari sf90 stradale\n80т.р./сут";
    private static final String HEADER_AUTO = "### Раздел аренды автомобилей ###";
    private static final String HEADER_APT = "### Раздел аренды Квартир ###";
    private static final String BACK = "<В начало|start";
    private static final String DONE = "Ваша зявка принята.\n\n В ближайшее время с вами свяжется менеджер для подтверждения бранирования.";
    private static final String HELLO_MSG = "RentGuyBot - бот по аренде автомобией и жилья. \n\nВыбирай и бранируй автомобили и жилье в лбое время.\n\nЧтобы начать выберите категорию:";
    private static final String PRICE_MSG = "Введите диапазон цен \nв формате 1000-100000:";
    private static final String START_CASE = "start";
    private static final String CAR_CASE = "car";
    private static final String APARTMENT_CASE = "apartment";
    private static final String INPUT_DATES_CASE = "inputDates";
    private static final String ACCEPT_DATES_CASE = "acceptDates";
    private static final String ACCEPT_BEDROOM_CASE = "acceptBedRooms";
    private static final String ACCEPT_PRICE_CASE = "acceptPrice";
    private static final String RENT_CASE = "rent";
    private static final String VACANCY_ORDER = "Доступные варианты";

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
            case START_CASE:
                callBackAnswer = getMainMenuMessage(chatId,userId, buttonQuery.getMessage().getReplyMarkup());
                break;
            case CAR_CASE:
                callBackAnswer = getCallBackMenu(chatId,HEADER_AUTO+"\n\nСледуйте пунктам меню:", List.of("Ввести даты начала/окончания аренды|" + INPUT_DATES_CASE,"< Назад|start"));
                botStateCash.saveBotState(userId,BotState.CAR);
                break;
            case APARTMENT_CASE:
                callBackAnswer = getCallBackMenu(chatId,HEADER_APT+"\n\nСледуйте пунктам меню:", List.of("Ввести даты заезда/выезда|" + INPUT_DATES_CASE,"< Назад|start"));
                botStateCash.saveBotState(userId,BotState.APARTMENT);
                break;
            case INPUT_DATES_CASE:
                callBackAnswer = new SendMessage(
                        String.valueOf(chatId),
                        (BotState.APARTMENT.name().equals(botStateCash.getBotStateMap().get(userId).name()) ? HEADER_APT:HEADER_AUTO) +
                                "\n\nВведите даты начала и окончания аренды в формате \n\nДД.ММ.ГГГГ-ДД.ММ.ГГГГ");
                messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_DATES);
                break;
            case ACCEPT_DATES_CASE:
                if (BotState.APARTMENT.equals(botStateCash.getBotStateMap().get(userId))) {
                    callBackAnswer = new SendMessage(String.valueOf(chatId), "Введите количество комнат");
                    messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_BEDROOMS);
                } else {
                    callBackAnswer = new SendMessage(String.valueOf(chatId), PRICE_MSG);
                    messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_PRICES);
                }
                break;
            case ACCEPT_BEDROOM_CASE:
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText(VACANCY_ORDER);
                sendMessage.setReplyMarkup(getInlineMessageButtons(List.of(TEXT_1, TEXT_2, TEXT_3, BACK)));
                callBackAnswer = sendMessage;
                messageHandlerCash.saveBotCategoryState(userId, BotState.CAR_LIST);
                break;
            case ACCEPT_PRICE_CASE:
                SendMessage sendMessage1 = new SendMessage();
                sendMessage1.setChatId(String.valueOf(chatId));
                sendMessage1.setText(VACANCY_ORDER);
                sendMessage1.setReplyMarkup(getInlineMessageButtons(List.of(TEXT_4, TEXT_5, TEXT_6, BACK)));
                callBackAnswer = sendMessage1;
                messageHandlerCash.saveBotCategoryState(userId, BotState.CAR_LIST);
                break;
            case RENT_CASE:
                SendMessage sendMessage3 = new SendMessage();
                sendMessage3.setChatId(String.valueOf(chatId));
                sendMessage3.setText(DONE);
                sendMessage3.setReplyMarkup(getInlineMessageButtons(List.of(BACK)));
                callBackAnswer = sendMessage3;
                messageHandlerCash.saveBotCategoryState(userId, BotState.START);
                break;
            default:
                SendMessage sendMessage2 = new SendMessage();
                sendMessage2.setChatId(String.valueOf(chatId));
                sendMessage2.setText(data);
                if (BotState.APARTMENT.equals(botStateCash.getBotStateMap().get(userId))) {
                    sendMessage2.setReplyMarkup(getInlineMessageButtons(List.of("Забронировать|"+RENT_CASE,"< Назад|"+ACCEPT_BEDROOM_CASE)));
                } else {
                    sendMessage2.setReplyMarkup(getInlineMessageButtons(List.of("Забронировать|"+RENT_CASE,"< Назад|"+ACCEPT_PRICE_CASE)));
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
        replyMessage.setText(HELLO_MSG);
        replyMessage.setReplyMarkup(getInlineMessageButtons1(userId,inlineKeyboardMarkup));
        return replyMessage;
    }

    public InlineKeyboardMarkup getInlineMessageButtons1(final long userId, InlineKeyboardMarkup inlineKeyboardMarkup) {

        InlineKeyboardButton carBtn = new InlineKeyboardButton();
        carBtn.setText("Автомобиль");
        carBtn.setCallbackData(CAR_CASE);

        InlineKeyboardButton apartmentBtn = new InlineKeyboardButton();
        apartmentBtn.setText("Квартира");
        apartmentBtn.setCallbackData(APARTMENT_CASE);

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
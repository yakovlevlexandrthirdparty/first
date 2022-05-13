package ru.skb.rentguy.first.telegram.handlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.skb.rentguy.first.cash.AdvertCash;
import ru.skb.rentguy.first.cash.BotStateCash;
import ru.skb.rentguy.first.cash.MessageHandlerCash;
import ru.skb.rentguy.first.entities.Advert;
import ru.skb.rentguy.first.model.BotState;
import ru.skb.rentguy.first.repositories.AdvertRepository;
import ru.skb.rentguy.first.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CallbackQueryHandler {
    private static final String TEXT_1 = "Садовыая-Кудринская д.6: 3100Р/сут.::Просторный апартамент\n20т.р./сут";
    private static final String TEXT_2 = "Кривоколейный пер. д.12: 2000Р/сут.::Апартамент с видом на воду\n41т.р./сут";
    private static final String TEXT_3 = "Лужниковсктй пер. д.22: 2800Р/сут.::Апартамент с терассой\n80т.р./сут";
    private static final String TEXT_4 = "Mercedes-Benz S63 coupe 20т.р./сут::Mercedes-Benz S63 coupe\n20т.р./сут";
    private static final String TEXT_5 = "Lamborghini aventador svj 41т.р./сут::Lamborghini aventador svj\n41т.р./сут";
    private static final String TEXT_6 = "Ferrari sf90 stradale 80т.р./сут::Ferrari sf90 stradale\n80т.р./сут";
    private static final String HEADER_AUTO = "\uD83C\uDFCE Раздел аренды автомобилей\n\nТут вы можете посмотреть доступные автомобили эконом, бзнес и премиум класса.";
    private static final String HEADER_APT = "\uD83C\uDFE1 Раздел аренды Квартир\n\nТут вы можете посмотреть доступныое жилье эконом, бзнес и премиум класса.";
    private static final String BACK = "<В начало::start";
    private static final String DONE = "✅ Ваша зявка принята.\n\nВ ближайшее время с вами свяжется наш менеджер для подтверждения бранирования.\n\nСпасибо что воспользовались нашим сервисом.\uD83E\uDD70";
    private static final String HELLO_MSG = "RentGuyBot®️\nБот по аренде автомобией и жилья в Сочи.\n\nВыбирай и бранируй автомобили и жилье в лбое время.\nБот это быстро, легко и всегда подрукой.\n\n\uD83D\uDC47\uD83C\uDFFDЧтобы начать выберите категорию\uD83D\uDC47\uD83C\uDFFD";
    private static final String PRICE_MSG = "Введите диапазон цен\nв формате 1000-100000:";
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
    @Autowired
    private final AdvertCash advertCash;
    @Autowired
    private final AdvertRepository advertRepository;
    @Autowired
    private final UserRepository userRepository;

    public BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery) {
        final long chatId = buttonQuery.getMessage().getChatId();
        final long userId = buttonQuery.getFrom().getId();
        final Integer msgId = buttonQuery.getMessage().getMessageId();

        BotApiMethod<?> callBackAnswer = null;

        String data = buttonQuery.getData(); //TODO: split data to command and payload
        switch (data) {
            case START_CASE:
                callBackAnswer = getMainMenuMessage(buttonQuery.getMessage());
                break;
            case "backT":
                callBackAnswer = getCallBackMenu2(msgId, chatId, HELLO_MSG, List.of("\uD83C\uDFCE Автомобиль::car", "\uD83C\uDFE1 Жильё::apartment"));
                break;
            case CAR_CASE:
                callBackAnswer = getCallBackMenu(msgId, chatId, HEADER_AUTO + "\n\nСледуйте пунктам меню:", List.of("Ввести даты начала/окончания аренды::" + INPUT_DATES_CASE, "\uD83D\uDCB8 Создать обьявление::makeAdvert", "<Назад::backT"));
                botStateCash.saveBotState(userId, BotState.CAR);
                break;
            case "makeAdvert":
                callBackAnswer = getCallBackMenu(msgId, chatId, "Введите заголовок обьявления:\n\n(Максимум 150 символов)", List.of("<Назад::backT"));
                messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_ADVERT_TITLE);
                advertCash.saveAdvert(userId, new Advert());
                break;
            case APARTMENT_CASE:
                callBackAnswer = getCallBackMenu(msgId, chatId, HEADER_APT + "\n\nСледуйте пунктам меню:", List.of("Ввести даты заезда/выезда::" + INPUT_DATES_CASE, "<Назад::backT"));
                botStateCash.saveBotState(userId, BotState.APARTMENT);
                break;
            case INPUT_DATES_CASE:
                callBackAnswer = new SendMessage(
                        String.valueOf(chatId),
                        //(BotState.APARTMENT.name().equals(botStateCash.getBotStateMap().get(userId).name()) ? HEADER_APT : HEADER_AUTO) +
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
                callBackAnswer = EditMessageReplyMarkup.builder()
                        .chatId(String.valueOf(chatId))
                        .messageId(msgId)
                        .replyMarkup(getInlineMessageButtons(List.of(TEXT_1, TEXT_2, TEXT_3, BACK)))
                        .build();
                messageHandlerCash.saveBotCategoryState(userId, BotState.CAR_LIST);
                break;
            case ACCEPT_PRICE_CASE:
                List<Advert> adverts = (List<Advert>) advertRepository.findAll();
                List<String> advertBtnList = adverts.stream()
                        .filter(el -> userRepository.findByTelegramId(userId).getId() != el.getAuthorId())
                        .map(el -> {
                            return el.getTitle() + "::" + el.getTitle() + "\n" + el.getPrice();
                        })
                        .collect(Collectors.toList());
                advertBtnList.add(BACK);
                callBackAnswer = EditMessageReplyMarkup.builder()
                        .chatId(String.valueOf(chatId))
                        .messageId(msgId)
                        .replyMarkup(getInlineMessageButtons(advertBtnList))
                        .build();
                messageHandlerCash.saveBotCategoryState(userId, BotState.CAR_LIST);
                break;
            case RENT_CASE:

                callBackAnswer = EditMessageText.builder()
                        .chatId(String.valueOf(chatId))
                        .messageId(msgId)
                        .text(DONE)
                        .replyMarkup(getInlineMessageButtons(List.of(BACK)))
                        .build();
                messageHandlerCash.saveBotCategoryState(userId, BotState.START);
                break;
            default:
                SendMessage sendMessage2 = new SendMessage();
                sendMessage2.setChatId(String.valueOf(chatId));
                sendMessage2.setText(data);
                if (BotState.APARTMENT.equals(botStateCash.getBotStateMap().get(userId))) {
                    sendMessage2.setReplyMarkup(getInlineMessageButtons(List.of("Забронировать::" + RENT_CASE, "<Назад::" + ACCEPT_BEDROOM_CASE)));
                } else {
                    sendMessage2.setReplyMarkup(getInlineMessageButtons(List.of("Забронировать::" + RENT_CASE, "<Назад::" + ACCEPT_PRICE_CASE)));
                }
                callBackAnswer = sendMessage2;
                break;
        }
        return callBackAnswer;
    }

    public EditMessageText getCallBackMenu(Integer msgId, final long chatId, String header, List<String> menuItems) {
        return EditMessageText.builder()
                .chatId(String.valueOf(chatId))
                .messageId(msgId)
                .replyMarkup(getInlineMessageButtons(menuItems))
                .text(header)
                .build();
    }

    public EditMessageText getCallBackMenu2(Integer msgId, final long chatId, String header, List<String> menuItems) {
        return EditMessageText.builder()
                .chatId(String.valueOf(chatId))
                .messageId(msgId)
                .text(header)
                .replyMarkup(getInlineMessageButtons1())
                .build();
    }

    public SendMessage getMainMenuMessage(Message message) {
        return SendMessage.builder()
                .chatId(String.valueOf(message.getChatId()))
                .text(HELLO_MSG)
                .replyMarkup(getInlineMessageButtons1())
                .build();
    }

    public InlineKeyboardMarkup getInlineMessageButtons1() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
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
        List<List<InlineKeyboardButton>> rowList2 = btnList.stream()
                .map(btnString -> {
                    String[] pl = btnString.split("::");
                    InlineKeyboardButton i = InlineKeyboardButton.builder()
                            .text(pl[0])
                            .callbackData(pl[1])
                            .build();
                    List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
                    keyboardButtonsRow1.add(i);
                    return keyboardButtonsRow1;
                }).collect(Collectors.toList());
        return InlineKeyboardMarkup.builder()
                .keyboard(rowList2)
                .build();
    }
}
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
import ru.skb.rentguy.first.cash.AdvertCash;
import ru.skb.rentguy.first.cash.BotStateCash;
import ru.skb.rentguy.first.cash.MessageHandlerCash;
import ru.skb.rentguy.first.entities.Advert;
import ru.skb.rentguy.first.entities.User;
import ru.skb.rentguy.first.model.BotState;
import ru.skb.rentguy.first.repositories.AdvertRepository;
import ru.skb.rentguy.first.repositories.UserRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    @Autowired
    AdvertCash advertCash;
    @Autowired
    AdvertRepository advertRepository;
    @Autowired
    UserRepository userRepository;

    public BotApiMethod<?> handle(Message message, BotState botState, BotState msgHandlerStage) {
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();
        botStateCash.saveBotState(userId, botState);
        messageHandlerCash.saveBotCategoryState(userId, msgHandlerStage);
        switch (msgHandlerStage.name()) {
            case "START":
                return callbackQueryHandler.getMainMenuMessage(message);
            case "INPUT_ADVERT_TITLE":
                if (message.getText().length() < 150) {
                    Advert advert = advertCash.getAdvertMap().get(userId);
                    advert.setTitle(message.getText());
                    advert.setAuthorId(userRepository.findByTelegramId(userId).getId());
                    advertCash.saveAdvert(userId, advert);
                    messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_ADVERT_URL);
                    return SendMessage.builder()
                            .chatId(String.valueOf(message.getChatId()))
                            .text("??????????????????: " + advertCash.getAdvertMap().get(userId).getTitle() + "\n???????????????? ???????????? ???? ??????????????????????.")
                            .replyMarkup(getInlineMessageButtons(List.of("start::<?? ?????????????? ????????")))
                            .build();
                } else {
                    throw new IllegalArgumentException("?????????????????? ?????????????? ??????????????.");
                }
            case "INPUT_ADVERT_URL":
                Advert advert2 = advertCash.getAdvertMap().get(userId);
                if(true){
                    advert2.setDescription(message.getText());
                    messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_ADVERT_PRICE);
                    return SendMessage.builder()
                            .chatId(String.valueOf(message.getChatId()))
                            .text("??????????????????: " + advertCash.getAdvertMap().get(userId).getTitle() + "\n?????????????? ?????????????????? ???????????? ?? ??????????:\n\n???????????? ??????????")
                            .replyMarkup(getInlineMessageButtons(List.of("start::<?? ?????????????? ????????")))
                            .build();
                } else {
                    throw new IllegalArgumentException("???????????????????????? ???????????? ????????????, ???????????????? ??????.");
                }
            case "INPUT_ADVERT_PRICE":
                Advert advert1 = advertCash.getAdvertMap().get(userId);
                if (message.getText().matches("^[0-9]+$")) {
                    advert1.setPrice(Double.parseDouble(message.getText()));
                    advertCash.saveAdvert(userId, advert1);
                    Advert advert = advertRepository.save(advert1);
                    System.out.println(">>" + advert.getAuthorId() + " " + advert.getTitle() + " " + advert.getPrice());
                    advertCash.getAdvertMap().values().forEach(System.out::println);
                    return SendMessage.builder()
                            .chatId(String.valueOf(message.getChatId()))
                            .text("??? ?????????????? ???????????????????? ??????????????!\n\n" + "??????????????????: " + advert1.getTitle() + "\n????????: " + advert1.getPrice() + " ??????./??????.")
                            .replyMarkup(getInlineMessageButtons(List.of("start::<?? ?????????????? ????????")))
                            .build();
                } else {
                    throw new IllegalArgumentException("???????????????? ???????????????? ???????????????????? ?????? ??????.");
                }
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
            case "EX":
                return SendMessage.builder()
                        .chatId(String.valueOf(chatId))
                        .text("V")
                        .build();
            default:
                throw new IllegalStateException("Unexpected value: " + botState);
        }
    }

    public BotApiMethod<?> enterDates(Message message, BotState msgHandlerStage) {
        long userId = message.getFrom().getId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String stringStartDate = message.getText();
        try {
            Date startDate = simpleDateFormat.parse(stringStartDate);
            sendMessage.setText("???????? ???????????? ????????????: " + new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(startDate));
        } catch (ParseException e) {
            sendMessage.setText("???????????????????? ????????????, ???????????????????? ?????? ??????");
            e.printStackTrace();
        }
        sendMessage.setReplyMarkup(getInlineMessageButtons(List.of("acceptDates::????, ???????? ??????????")));
        if (msgHandlerStage.equals(BotState.APARTMENT)) {
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
        sendMessage.setText("?????????????????????? ???????????????????? ????????????: " + message.getText());
        sendMessage.setReplyMarkup(getInlineMessageButtons(List.of("????, ???????????????????? ???????????? ??????????::acceptBedRooms")));
        messageHandlerCash.saveBotCategoryState(userId, BotState.APARTMENTS_LIST);
        return sendMessage;
    }

    public BotApiMethod<?> enterPrices(Message message) {
        long userId = message.getFrom().getId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText("?????????????????????? ???????????????? ??????: ???? " + message.getText() + "???");
        sendMessage.setReplyMarkup(getInlineMessageButtons(List.of("acceptPrice::????, ???????????????? ?????? ????????????")));
        messageHandlerCash.saveBotCategoryState(userId, BotState.APARTMENTS_LIST);
        return sendMessage;
    }

    public InlineKeyboardMarkup getInlineMessageButtons(List<String> btnList) {
        List<List<InlineKeyboardButton>> rowList2 = btnList.stream()
                .map(btnString -> {
                    String[] pl = btnString.split("::");
                    InlineKeyboardButton i = InlineKeyboardButton.builder()
                            .text(pl[1])
                            .callbackData(pl[0])
                            .build();
                    List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
                    keyboardButtonsRow1.add(i);
                    return keyboardButtonsRow1;
                }).collect(Collectors.toList());
        return InlineKeyboardMarkup.builder()
                .keyboard(rowList2)
                .build();
    }

    private SendMessage getSendMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(chatId, "?????????????????????? ???????????? ????????.");
        return sendMessage;
    }

}
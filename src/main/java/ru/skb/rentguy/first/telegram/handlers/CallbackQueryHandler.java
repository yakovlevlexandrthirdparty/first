package ru.skb.rentguy.first.telegram.handlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.skb.rentguy.first.cash.AdvertCash;
import ru.skb.rentguy.first.cash.BotStateCash;
import ru.skb.rentguy.first.cash.MessageHandlerCash;
import ru.skb.rentguy.first.cash.OrderCash;
import ru.skb.rentguy.first.entities.Advert;
import ru.skb.rentguy.first.entities.Order;
import ru.skb.rentguy.first.entities.OrderDate;
import ru.skb.rentguy.first.entities.User;
import ru.skb.rentguy.first.model.BotState;
import ru.skb.rentguy.first.repositories.AdvertRepository;
import ru.skb.rentguy.first.repositories.OrderDateRepository;
import ru.skb.rentguy.first.repositories.OrderRepository;
import ru.skb.rentguy.first.repositories.UserRepository;
import ru.skb.rentguy.first.telegram.BtnUtils;
import ru.skb.rentguy.first.telegram.WriteReadBot;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CallbackQueryHandler {
    private static final String TEXT_1 = "???????????????????? ????????????????????\n20??.??./??????::????????????????-???????????????????? ??.6: 3100??/??????.";
    private static final String TEXT_2 = "???????????????????? ?? ?????????? ???? ????????\n41??.??./??????::?????????????????????????? ??????. ??.12: 2000??/??????.";
    private static final String TEXT_3 = "???????????????????? ?? ????????????????\n80??.??./??????::???????????????????????? ??????. ??.22: 2800??/??????.";
    private static final String HEADER_AUTO = "\uD83C\uDFE1 ???????????? ???????????? ??????????????\n\n?????? ???? ???????????? ???????????????????? ?????????????????? ?????????? ????????????, ?????????? ?? ?????????????? ????????????.";
    private static final String HEADER_APT = "\uD83C\uDFE1 ???????????? ???????????? ??????????????\n\n?????? ???? ???????????? ???????????????????? ???????????????????? ?????????? ????????????, ?????????? ?? ?????????????? ????????????.";
    private static final String BACK = "start::<?? ????????????";
    private static final String DONE = "??? ???????? ?????????? ??????????????.\n\n?? ?????????????????? ?????????? ?? ???????? ???????????????? ?????? ???????????????? ?????? ?????????????????????????? ????????????????????????.\n\n?????????????? ?????? ?????????????????????????????? ?????????? ????????????????.\uD83E\uDD70";
    private static final String HELLO_MSG = "RentGuyBot?????\n?????? ???? ???????????? ???????????????????? ?? ?????????? ?? ????????.\n\n?????????????? ?? ???????????????? ???????????????????? ?? ?????????? ?? ???????? ??????????.\n?????? ?????? ????????????, ?????????? ?? ???????????? ????????????????.\n\n\uD83D\uDC47\uD83C\uDFFD?????????? ???????????? ???????????????? ??????????????????\uD83D\uDC47\uD83C\uDFFD";
    private static final String PRICE_MSG = "?????????????? ???????????????? ??????\n?? ?????????????? 1000-100000:";
    private static final String START_CASE = "start";
    private static final String CAR_CASE = "car";
    private static final String APARTMENT_CASE = "apartment";
    private static final String INPUT_DATES_CASE = "inputDates";
    private static final String ACCEPT_DATES_CASE = "acceptDates";
    private static final String ACCEPT_BEDROOM_CASE = "acceptBedRooms";
    private static final String ACCEPT_PRICE_CASE = "acceptPrice";
    private static final String RENT_CASE = "rent#";
    private static final String VACANCY_ORDER = "?????????????????? ????????????????";

    public CallbackQueryHandler(BotStateCash botStateCash, MessageHandlerCash messageHandlerCash, AdvertCash advertCash, AdvertRepository advertRepository, UserRepository userRepository, OrderDateRepository orderDateRepository, OrderRepository orderRepository, OrderCash orderCash, @Lazy WriteReadBot writeReadBot) {
        this.botStateCash = botStateCash;
        this.messageHandlerCash = messageHandlerCash;
        this.advertCash = advertCash;
        this.advertRepository = advertRepository;
        this.userRepository = userRepository;
        this.orderDateRepository = orderDateRepository;
        this.orderRepository = orderRepository;
        this.orderCash = orderCash;
        this.writeReadBot = writeReadBot;
    }

    private final BotStateCash botStateCash;

    private final MessageHandlerCash messageHandlerCash;

    private final AdvertCash advertCash;

    private final AdvertRepository advertRepository;

    private final UserRepository userRepository;

    private final OrderDateRepository orderDateRepository;

    private final OrderRepository orderRepository;

    private final OrderCash orderCash;

    private final WriteReadBot writeReadBot;
    private List<Order> orders2 = new ArrayList();
    private Map<Long, List<Order>> userOrders = new HashMap<>();

    @SneakyThrows
    public BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery) {
        System.out.println("processCallbackQuery.data>>" + buttonQuery.getData());
        final long chatId = buttonQuery.getMessage().getChatId();
        final long userId = buttonQuery.getFrom().getId();
        final Integer msgId = buttonQuery.getMessage().getMessageId();

        BotApiMethod<?> callBackAnswer = null;

        String data = buttonQuery.getData();
        String[] dataArr = data.split("::");
        switch (dataArr[0]) {
            case START_CASE:
                callBackAnswer = getMainMenuMessage(buttonQuery.getMessage());
                break;
            case "backT":
                callBackAnswer = getCallBackMenu2(msgId, chatId, HELLO_MSG, List.of("car::\uD83C\uDFCE ????????????????????", "apartment::\uD83C\uDFE1 ??????????"));
                break;
            case "adverts":
                List<Advert> advertList = (List<Advert>) advertRepository.findAll();
                List<String> btns = advertList.stream().filter(advert -> !userRepository.findById(advert.getAuthorId()).orElseThrow().getTelegramId().equals(userId)).map(advert -> {
                    return "advert#" + advert.getId() + "::" + advert.getTitle();
                }).collect(Collectors.toList());
                btns.add("car::<??????????");
                //TODO : if has advert add string ???????? ???????? n ????????????????????(??), ?????? ?????? ?? ????????????
                callBackAnswer = getCallBackMenu(msgId, chatId, "\uD83C\uDFE1 ?????????????????? ?????????? ????????????, ???????????? ?? ?????????????? ????????????", btns);
                break;
            case "car":
                if (orders2.isEmpty()) {
                    orders2 = (List<Order>) orderRepository.findAll();
                }
                Long userIdD = userRepository.findByTelegramId(userId).getId();
                if (!userOrders.containsKey(userId)) {
                    userOrders.put(userId, orders2.stream().filter(el -> {
                        return advertRepository.findById(el.getAdvertId()).orElseThrow().getAuthorId() == userIdD;
                    }).collect(Collectors.toList()));
                }
                List<String> btnList1 = new ArrayList<>();
                btnList1.add("adverts::????????????????????");
                btnList1.add("makeAdvert::\uD83D\uDCB8 ?????????????? ????????????????????");
                if (!userOrders.get(userId).isEmpty()) {
                    btnList1.add("myOrders::\uD83D\uDCC4 ????????????");
                }
                btnList1.add("backT::<??????????");
                callBackAnswer = getCallBackMenu(msgId, chatId, HEADER_AUTO + "\n\n???????????????? ?????????????? ????????:", btnList1);
                botStateCash.saveBotState(userId, BotState.CAR);
                break;
            case "myOrders":
                List<Order> orderList = userOrders.get(userId).stream().filter(order -> order.getOrderDates().size() > 0).collect(Collectors.toList());
                List<String> btnList = orderList.stream().map(order -> {
                    String title = advertRepository.findById(order.getAdvertId()).orElseThrow().getTitle();
                    return "order#" + order.getId() + "#" + userRepository.findById(order.getRecipientId()).orElseThrow().getUserName() + "::???" + order.getId() + " " + title + " " + BtnUtils.daysNaming(order.getOrderDates().size()) + " " + BtnUtils.paidOrder(order);
                }).collect(Collectors.toList());

                btnList.add("car::<??????????");
                callBackAnswer = getCallBackMenu(msgId, chatId, "?????????????? ?? ?????????? ??????????????????????:", btnList);
                break;
            case "makeAdvert":
                callBackAnswer = getCallBackMenu(msgId, chatId, "?????????????? ?????????????????? ????????????????????:\n\n(???????????????? 150 ????????????????)", List.of("backT::<??????????"));
                messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_ADVERT_TITLE);
                advertCash.saveAdvert(userId, new Advert());
                break;
            case "hi":
                callBackAnswer = getCallBackMenu(msgId, chatId, "", List.of("C"));
            case APARTMENT_CASE:
                callBackAnswer = getCallBackMenu(msgId, chatId, HEADER_APT + "\n\n???????????????? ?????????????? ????????:", List.of("INPUT_DATES_CASE::???????????? ???????? ????????????", "backT::<??????????"));
                botStateCash.saveBotState(userId, BotState.APARTMENT);
                break;
            case INPUT_DATES_CASE:
                callBackAnswer = new SendMessage(
                        String.valueOf(chatId),
                        (BotState.APARTMENT.name().equals(botStateCash.getBotStateMap().get(userId).name()) ? HEADER_APT : HEADER_AUTO) +
                                "\n\n?????????????? ???????? ???????????? ???????????? ?? ?????????????? \n\n????.????.????????");
                messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_DATES);
                break;
            case ACCEPT_DATES_CASE:
                if (BotState.APARTMENT.equals(botStateCash.getBotStateMap().get(userId))) {
                    callBackAnswer = new SendMessage(String.valueOf(chatId), "?????????????? ???????????????????? ????????????");
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
                            return el.getId() + "::" + el.getTitle() + " \n" + el.getPrice() + "??. ";
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
            default:
                if (data.startsWith("done")) {
                    String[] doneDataArr = data.split("#");
                    Order order = orderCash.getOrderMap().get(userId);
                    order.setState(0);
                    order.setRecipientId(userRepository.findByTelegramId(userId).getId());
                    order.setAdvertId(Long.parseLong(doneDataArr[1]));
                    order = orderRepository.save(order);
                    orderDateRepository.saveAll(orderCash.getOrderMap().get(userId).getOrderDates());
                    callBackAnswer = EditMessageText.builder()
                            .chatId(String.valueOf(chatId))
                            .messageId(msgId)
                            .text("??? ???????? ?????????? #" + order.getId() + " ??????????????.\n\n?? ?????????????????? ?????????? ?? ???????? ???????????????? ?????? ???????????????? ?????? ?????????????????????????? ????????????????????????.\n\n?????????????? ?????? ?????????????????????????????? ?????????? ????????????????.\uD83E\uDD70")
                            .replyMarkup(getInlineMessageButtons(List.of("start::<??????????")))
                            .build();
                    User user = userRepository.findById(advertRepository.findById(order.getAdvertId()).get().getAuthorId()).orElseThrow();
                    writeReadBot.execute(SendMessage.builder()
                            .chatId(user.getTelegramId().toString())
                            .text("???? ???????????? ???????????????????? ???????????????? ??????????")
                            .build());
                }
                System.out.println("default data:" + buttonQuery.getData());
                String[] dataArr1 = data.split("#");
                if (data.startsWith("advert")) {
                    List<Order> orders = (List<Order>) orderRepository.findAll();
                    Advert ad = advertRepository.findById(Long.parseLong(dataArr1[1])).orElseThrow();
//                    if (ad.getTitle().contains("SF90")) {
//                        System.out.println("<<<<<<");
//                        writeReadBot.sendPhoto(String.valueOf(chatId), "C:\\Users\\PC12\\IdeaProjects\\first\\src\\main\\resources\\static\\60f7b38ffd2ee1629baa443e.jpg");
//                    }
                    if (orderCash.getOrderMap().get(userId) == null) {
                        Order order = new Order();
                        order.setRecipientId(userRepository.findByTelegramId(userId).getId());
                        order.setAdvertId(ad.getId());
                        order.setOrderDates(new ArrayList<OrderDate>());
                        orderCash.saveOrder(userId, order);
                        Order resOrder = orderRepository.save(order);
                    }
                    String advertId = ad.getId().toString();
                    List<String> btns1 = new ArrayList<>();
                    Date today = new Date();
                    Calendar cal = Calendar.getInstance();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(today);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.HOUR, 0);
                    today = calendar.getTime();
                    Set<Date> ordersDates = orderCash.getOrderMap().get(userId).getOrderDates().stream().filter(el -> advertId.equals(el.getAdvertId().toString())).map(OrderDate::getDate).collect(Collectors.toSet());
                    cal.setTime(today);


                    List<Order> paidOrders = orders.stream().filter(order -> order.getState() == 1 && order.getAdvertId() == Long.parseLong(advertId)).collect(Collectors.toList());
                    Set<OrderDate> paidOrderDates = new HashSet<>();
                    paidOrders.stream().map(Order::getOrderDates).forEach(paidOrderDates::addAll);
                    Set<Date> datePaidOrderDates = paidOrderDates.stream().map(OrderDate::getDate).collect(Collectors.toSet());
                    for (int i = 0; i < 10; i++) {
                        if (!datePaidOrderDates.contains(cal.getTime())) {
                            btns1.add(ordersDates.contains(cal.getTime()) ? "rent#" + advertId + "#" + cal.getTimeInMillis() + "::???" + cal.getTime() : "rent#" + advertId + "#" + cal.getTimeInMillis() + "::" + cal.getTime());
                        }
                        cal.add(Calendar.DATE, 1);
                    }
                    btns1.add("adverts::<??????????");
//                    File file = ResourceUtils.getFile(fileName);
//                    InputFile inputFile = new InputFile(file);
//                    SendPhoto sp = new SendPhoto();
//                    sp.setPhoto(inputFile);
//                    sp.setChatId(chatId);
//                    InputMedia inputMedia = new InputMediaPhoto();
//                    inputMedia.setMedia(file,"s.png");
//                    inputMedia.setCaption(ad.getTitle());
//                    callBackAnswer = EditMessageMedia.builder()
//                            .chatId(String.valueOf(chatId))
//                            .messageId(msgId)
//                            .media(inputMedia)
//                            .replyMarkup(getInlineMessageButtons(btns1))
//                            .build();
                    callBackAnswer = EditMessageText.builder()
                            .chatId(String.valueOf(chatId))
                            .messageId(msgId)
                            .text(ad.getDescription() + "\n" + ad.getTitle() + "\n" + ad.getPrice() + " ??????./??????.\n\n" + "???????????????? ?????????????????? ????????\uD83D\uDC47\uD83C\uDFFD")
                            .replyMarkup(getInlineMessageButtons(btns1))
                            .build();
                    break;
                }
                if (data.startsWith("paid")) {
                    Order order = orderRepository.findById(Long.parseLong(dataArr1[1])).orElseThrow();
                    order.setState(1);
                    orderRepository.save(order);
                    callBackAnswer = EditMessageText.builder()
                            .chatId(String.valueOf(chatId))
                            .messageId(msgId)
                            .text("??? ???? ?????????????????????? ???????????? ???????????? #" + order.getId() + "\n\n???????? ???? ???????????? ?????????????? ???? ?????????????????? ?? ????????????????????.")
                            .replyMarkup(getInlineMessageButtons(List.of("start::<??????????")))
                            .build();
                }
                if (data.startsWith("order")) {
                    Order order = orderRepository.findById(Long.parseLong(dataArr1[1])).orElseThrow();
                    String title = advertRepository.findById(order.getAdvertId()).orElseThrow().getTitle();
                    String startdate = order.getOrderDates().iterator().next().getDate().toString();
                    List<String> btnsList = new ArrayList<>();
                    if (order.getState() == 0) {
                        btnsList.add("paid#" + order.getId() + "::\uD83D\uDC49\uD83C\uDFFD????????????????\uD83D\uDC48\uD83C\uDFFD");
                    }
                    btnsList.add("myOrders::<??????????");
                    callBackAnswer = EditMessageText.builder()
                            .chatId(String.valueOf(chatId))
                            .messageId(msgId)
                            .text("?????????????????????? ????????????:\n\n" + "??????:" + title + "\n\n???????? ???????????? ????????????: " + startdate + "\n\n?????????????? ?????? ??????????: @" + dataArr1[2] + BtnUtils.orderMessagePostfix(order))
                            .replyMarkup(getInlineMessageButtons(btnsList))
                            .build();
                    break;
                }
                if (data.startsWith("rent")) {
                    List<Order> orders3 = (List<Order>) orderRepository.findAll();
                    String advertId = dataArr1[1];
                    String dateFromBtn = dataArr1[2];
                    Date dateBtn = new Date(Long.parseLong(dateFromBtn));
                    Advert ad = advertRepository.findById(Long.parseLong(advertId)).orElseThrow();
                    OrderDate orderDate = new OrderDate();
                    Date today = new Date();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(today);
                    calendar.set(Calendar.MILLISECOND, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.HOUR, 0);
                    today = calendar.getTime();
                    orderDate.setDate(dateBtn);
                    orderDate.setState(0);
                    orderDate.setRecipientId(userRepository.findByTelegramId(userId).getId());
                    orderDate.setAdvertId(Long.parseLong(advertId));
                    Order order = orderCash.getOrderMap().get(userId);
                    orderDate.setOrder(order);
                    order.getOrderDates().add(orderDate);
                    Set<Date> ordersDates = orderCash.getOrderMap().get(userId).getOrderDates().stream().filter(el -> advertId.equals(el.getAdvertId().toString())).map(OrderDate::getDate).collect(Collectors.toSet());
                    List<String> btns1 = new ArrayList<>();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(today);

                    List<Order> paidOrders = orders3.stream().filter(ord -> ord.getState() == 1 && ord.getAdvertId() == Long.parseLong(advertId)).collect(Collectors.toList());
                    Set<OrderDate> paidOrderDates = new HashSet<>();
                    paidOrders.stream().map(Order::getOrderDates).forEach(paidOrderDates::addAll);
                    Set<Date> datePaidOrderDates = paidOrderDates.stream().map(OrderDate::getDate).collect(Collectors.toSet());
                    for (int i = 0; i < 10; i++) {
                        if (!datePaidOrderDates.contains(cal.getTime())) {
                            btns1.add(ordersDates.contains(cal.getTime()) ? "rent#" + advertId + "#" + cal.getTimeInMillis() + "::???" + cal.getTime() : "rent#" + advertId + "#" + cal.getTimeInMillis() + "::" + cal.getTime());
                        }
                        cal.add(Calendar.DATE, 1);
                    }

                    btns1.add("done#" + advertId + "::????????????");
                    btns1.add("adverts::<??????????");
                    callBackAnswer = EditMessageText.builder()
                            .chatId(String.valueOf(chatId))
                            .messageId(msgId)
                            .replyMarkup(getInlineMessageButtons(btns1))
                            .text(ad.getDescription() + "\n" + ad.getTitle() + "\n" + ad.getPrice() + " ??????./??????.\n\n" + "???????????????? ?????????????????? ????????\uD83D\uDC47\uD83C\uDFFD")
                            .build();
                    messageHandlerCash.saveBotCategoryState(userId, BotState.START);
                    break;
                }
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
        carBtn.setText("??????????");
        carBtn.setCallbackData(CAR_CASE);

        InlineKeyboardButton apartmentBtn = new InlineKeyboardButton();
        apartmentBtn.setText("????????????????");
        apartmentBtn.setCallbackData(APARTMENT_CASE);

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(carBtn);
        //keyboardButtonsRow1.add(apartmentBtn);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);

        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getInlineMessageButtons(List<String> btnList) {
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
}
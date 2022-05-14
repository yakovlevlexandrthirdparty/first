package ru.skb.rentguy.first.telegram.handlers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.aspectj.weaver.ast.Or;
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
import ru.skb.rentguy.first.cash.OrderCash;
import ru.skb.rentguy.first.entities.Advert;
import ru.skb.rentguy.first.entities.Order;
import ru.skb.rentguy.first.entities.OrderDate;
import ru.skb.rentguy.first.model.BotState;
import ru.skb.rentguy.first.repositories.AdvertRepository;
import ru.skb.rentguy.first.repositories.OrderDateRepository;
import ru.skb.rentguy.first.repositories.OrderRepository;
import ru.skb.rentguy.first.repositories.UserRepository;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CallbackQueryHandler {
    private static final String TEXT_1 = "Просторный апартамент\n20т.р./сут::Садовыая-Кудринская д.6: 3100Р/сут.";
    private static final String TEXT_2 = "Апартамент с видом на воду\n41т.р./сут::Кривоколейный пер. д.12: 2000Р/сут.";
    private static final String TEXT_3 = "Апартамент с терассой\n80т.р./сут::Лужниковсктй пер. д.22: 2800Р/сут.";
    private static final String TEXT_4 = "Mercedes-Benz S63 coupe\n20т.р./сут::Mercedes-Benz S63 coupe 20т.р./сут";
    private static final String TEXT_5 = "Lamborghini aventador svj\n41т.р./сут::Lamborghini aventador svj 41т.р./сут";
    private static final String TEXT_6 = "Ferrari sf90 stradale 80т.р./сут::Ferrari sf90 stradale\n80т.р./сут";
    private static final String HEADER_AUTO = "\uD83C\uDFCE Раздел аренды автомобилей\n\nТут вы можете посмотреть доступные автомобили эконом, бзнес и премиум класса.";
    private static final String HEADER_APT = "\uD83C\uDFE1 Раздел аренды Квартир\n\nТут вы можете посмотреть доступныое жилье эконом, бзнес и премиум класса.";
    private static final String BACK = "start::<В начало";
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
    private static final String RENT_CASE = "rent#";
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
    @Autowired
    private final OrderDateRepository orderDateRepository;
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final OrderCash orderCash;

    @SneakyThrows
    public BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery) {
        System.out.println("processCallbackQuery.data>>" + buttonQuery.getData());
        final long chatId = buttonQuery.getMessage().getChatId();
        final long userId = buttonQuery.getFrom().getId();
        final Integer msgId = buttonQuery.getMessage().getMessageId();

        BotApiMethod<?> callBackAnswer = null;

        String data = buttonQuery.getData();
        String[] dataArr = data.split("::");
        List<Order> orders = (List<Order>) orderRepository.findAll();
        List<Order> myOrders = orders.stream().filter(el -> {
            return advertRepository.findById(el.getAdvertId()).orElseThrow().getAuthorId() == userRepository.findByTelegramId(userId).getId();
        }).collect(Collectors.toList());
        switch (dataArr[0]) {
            case START_CASE:
                callBackAnswer = getMainMenuMessage(buttonQuery.getMessage());
                break;
            case "backT":
                callBackAnswer = getCallBackMenu2(msgId, chatId, HELLO_MSG, List.of("car::\uD83C\uDFCE Автомобиль", "apartment::\uD83C\uDFE1 Жильё"));
                break;
            case "adverts":
                List<Advert> advertList = (List<Advert>) advertRepository.findAll();
                List<String> btns = advertList.stream().filter(advert -> !userRepository.findById(advert.getAuthorId()).orElseThrow().getTelegramId().equals(userId)).map(advert -> {
                    return "advert#" + advert.getId() + "::" + advert.getTitle();
                }).collect(Collectors.toList());
                btns.add("car::<Назад");
                //TODO : if has advert add string Увас есть n обьявление(й), его нет в списке
                callBackAnswer = getCallBackMenu(msgId, chatId, "\uD83C\uDFCE Доступные автомобили эконом, бизнес и премиум класса", btns);
                break;
            case CAR_CASE:
                List<String> btnList1 = new ArrayList<>();
                btnList1.add("adverts::Объявления");
                btnList1.add("makeAdvert::\uD83D\uDCB8 Создать обьявление");
                if (!myOrders.isEmpty()) {
                    btnList1.add("myOrders::\uD83D\uDCC4 Ордера");
                }
                btnList1.add("backT::<Назад");
                callBackAnswer = getCallBackMenu(msgId, chatId, HEADER_AUTO + "\n\nСледуйте пунктам меню:", btnList1);
                botStateCash.saveBotState(userId, BotState.CAR);
                break;
            case "myOrders":

                // сделать значек оплачено, менять окончания и убрать заказы с 0 дней
                List<String> btnList = myOrders.stream().map(order -> {
                    String title = advertRepository.findById(order.getAdvertId()).orElseThrow().getTitle();
                    SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyyy");
                    return "order#" + order.getId() + "#" + userRepository.findById(order.getRecipientId()).orElseThrow().getUserName() + "::№" + order.getId() + " " + title + " " + order.getOrderDates().size() + " дней";
                }).collect(Collectors.toList());

                btnList.add("car::<Назад");
                callBackAnswer = getCallBackMenu(msgId, chatId, "Закявки к вашим обьявлениям:", btnList);
                break;
            case "makeAdvert":
                callBackAnswer = getCallBackMenu(msgId, chatId, "Введите заголовок обьявления:\n\n(Максимум 150 символов)", List.of("backT::<Назад"));
                messageHandlerCash.saveBotCategoryState(userId, BotState.INPUT_ADVERT_TITLE);
                advertCash.saveAdvert(userId, new Advert());
                break;
            case APARTMENT_CASE:
                callBackAnswer = getCallBackMenu(msgId, chatId, HEADER_APT + "\n\nСледуйте пунктам меню:", List.of("INPUT_DATES_CASE::Ввести даты заезда", "backT::<Назад"));
                botStateCash.saveBotState(userId, BotState.APARTMENT);
                break;
            case INPUT_DATES_CASE:
                callBackAnswer = new SendMessage(
                        String.valueOf(chatId),
                        (BotState.APARTMENT.name().equals(botStateCash.getBotStateMap().get(userId).name()) ? HEADER_APT : HEADER_AUTO) +
                                "\n\nВведите даты начала аренды в формате \n\nДД.ММ.ГГГГ");
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
                            return el.getId() + "::" + el.getTitle() + " \n" + el.getPrice() + "р. ";
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
                            .text("✅ Ваша зявка #" + order.getId() + " принята.\n\nВ ближайшее время с вами свяжется наш менеджер для подтверждения бранирования.\n\nСпасибо что воспользовались нашим сервисом.\uD83E\uDD70")
                            .replyMarkup(getInlineMessageButtons(List.of("start::<Назад")))
                            .build();
                }
                System.out.println("default data:" + buttonQuery.getData());
                String[] dataArr1 = data.split("#");
                if (data.startsWith("advert")) {
                    Advert ad = advertRepository.findById(Long.parseLong(dataArr1[1])).orElseThrow();
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
                    List<Order> paidOrders = orders.stream().filter(order -> order.getState() == 1).collect(Collectors.toList());
                    Set<OrderDate> paidOrderDates = new HashSet<>();
                    paidOrders.stream().map(Order::getOrderDates).forEach(paidOrderDates::addAll);
                    Set<Date> datePaidOrderDates = paidOrderDates.stream().map(OrderDate::getDate).collect(Collectors.toSet());
                    for (int i = 0; i < 10; i++) {
                        if (!datePaidOrderDates.contains(cal.getTime())) {
                            btns1.add(ordersDates.contains(cal.getTime()) ? "rent#" + advertId + "#" + cal.getTimeInMillis() + "::✅" + cal.getTime() : "rent#" + advertId + "#" + cal.getTimeInMillis() + "::" + cal.getTime());
                        }
                        cal.add(Calendar.DATE, 1);
                    }
                    btns1.add("adverts::<Назад");
                    callBackAnswer = EditMessageText.builder()
                            .chatId(String.valueOf(chatId))
                            .messageId(msgId)
                            .text(ad.getTitle() + "\n" + ad.getPrice() + " руб./сут.\n\n" + "Выбирите доступные даты\uD83D\uDC47\uD83C\uDFFD")
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
                            .text("✅ Вы подтвердили оплату заказа #" + order.getId() + "\n\nДаты по заявке исчезли из доступных в обьявлении.")
                            .replyMarkup(getInlineMessageButtons(List.of("start::<Назад")))
                            .build();
                }
                if (data.startsWith("order")) {
                    Order order = orderRepository.findById(Long.parseLong(dataArr1[1])).orElseThrow();
                    String title = advertRepository.findById(order.getAdvertId()).orElseThrow().getTitle();
                    String startdate = order.getOrderDates().iterator().next().getDate().toString();
                    callBackAnswer = EditMessageText.builder()
                            .chatId(String.valueOf(chatId))
                            .messageId(msgId)
                            .text("Подробности заявки:\n\n" + "Лот:" + title + "\n\nДата начала аренды:" + startdate + "\n\nНикнейм для связи: @" + dataArr1[2] + " \n\nКак получите оплату нажмите на кнопку \"Оплачено\" и даты исчезнут из списка доступных\uD83D\uDC47\uD83C\uDFFD")
                            .replyMarkup(getInlineMessageButtons(List.of("paid#" + order.getId() + "::\uD83D\uDC49\uD83C\uDFFDОплачено\uD83D\uDC48\uD83C\uDFFD", "myOrders::<Назад")))
                            .build();
                    break;
                }
                if (data.startsWith("rent")) {
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
                    orderDate.setOrder(orderCash.getOrderMap().get(userId));
                    orderCash.getOrderMap().get(userId).getOrderDates().add(orderDate);
                    Order order2 = orderCash.getOrderMap().get(userId);
                    order2.getOrderDates().add(orderDate);
                    Set<Date> ordersDates = orderCash.getOrderMap().get(userId).getOrderDates().stream().filter(el -> advertId.equals(el.getAdvertId().toString())).map(OrderDate::getDate).collect(Collectors.toSet());
                    List<String> btns1 = new ArrayList<>();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(today);
                    List<Order> paidOrders = orders.stream().filter(order -> order.getState() == 1).collect(Collectors.toList());
                    Set<OrderDate> paidOrderDates = new HashSet<>();
                    paidOrders.stream().map(Order::getOrderDates).forEach(paidOrderDates::addAll);
                    Set<Date> datePaidOrderDates = paidOrderDates.stream().map(OrderDate::getDate).collect(Collectors.toSet());
                    for (int i = 0; i < 10; i++) {
                        if (!paidOrderDates.contains(cal.getTime())) {
                            btns1.add(ordersDates.contains(cal.getTime()) ? "rent#" + advertId + "#" + cal.getTimeInMillis() + "::✅" + cal.getTime() : "rent#" + advertId + "#" + cal.getTimeInMillis() + "::" + cal.getTime());
                            cal.add(Calendar.DATE, 1);
                        }
                    }
                    btns1.add("done#" + advertId + "::Готово");
                    callBackAnswer = EditMessageText.builder()
                            .chatId(String.valueOf(chatId))
                            .messageId(msgId)
                            .replyMarkup(getInlineMessageButtons(btns1))
                            .text(ad.getTitle() + "\n" + ad.getPrice() + " руб./сут.\n\n" + "Выбирите доступные даты\uD83D\uDC47\uD83C\uDFFD")
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
package ru.skb.rentguy.first.telegram;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import ru.skb.rentguy.first.cash.BotStateCash;
import ru.skb.rentguy.first.cash.MessageHandlerCash;
import ru.skb.rentguy.first.constants.bot.BotMessageEnum;
import ru.skb.rentguy.first.entities.User;
import ru.skb.rentguy.first.exceptions.TelegramFileNotFoundException;
import ru.skb.rentguy.first.model.BotState;
import ru.skb.rentguy.first.repositories.UserRepository;
import ru.skb.rentguy.first.telegram.handlers.CallbackQueryHandler;
import ru.skb.rentguy.first.telegram.handlers.MessageHandler;

import java.io.*;
import java.io.File;
import java.util.*;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WriteReadBot extends SpringWebhookBot {
    private String botPath;
    private String botUsername;
    private String botToken;

    @Autowired
    TelegramApiClient telegramApiClient;

    @Autowired
    private BotStateCash botStateCash;

    @Autowired
    private MessageHandlerCash messageHandlerCash;

    @Autowired
    UserRepository userRepository;
    Iterable<User> users;
    Set<Long> usersSet = new HashSet<>();

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
            e.printStackTrace();
            return SendMessage.builder()
                    .chatId(update.hasMessage() ? String.valueOf(update.getMessage().getChatId()) : String.valueOf(update.getCallbackQuery().getFrom().getId()))
                    .text(BotMessageEnum.EXCEPTION_ILLEGAL_MESSAGE.getMessage() + " " + e.getMessage())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return SendMessage.builder()
                    .chatId(update.hasMessage() ? String.valueOf(update.getMessage().getChatId()) : String.valueOf(update.getCallbackQuery().getFrom().getId()))
                    .text(BotMessageEnum.EXCEPTION_WHAT_THE_FUCK.getMessage())
                    .build();
        }
    }


    public boolean checkUser(long userTelegramId){
         if(usersSet.contains(userTelegramId)){
             return true;
         } else {
             usersSet.add(userTelegramId);
             return false;
         }
    }

    private BotApiMethod<?> handleUpdate(Update update) {
        if(usersSet.isEmpty()){
            this.users = userRepository.findAll();
            for (User u:users) {
                usersSet.add(u.getTelegramId());
            }
        }
        if (update.hasMessage()) {
            long userId = update.getMessage().getFrom().getId();
            if (!checkUser(userId)) {
                System.out.println("NEW USER MS");
                User user = new User();
                user.setFirstName(update.getMessage().getFrom().getFirstName());
                user.setLastName(update.getMessage().getFrom().getLastName());
                user.setUserName(update.getMessage().getFrom().getUserName());
                user.setTelegramId(userId);
                User u = userRepository.save(user);
                System.out.println("user id:" + u.getId() + " " + u.getTelegramId());
            }
            return handleInputMessage(update.getMessage());
        }
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            long userId = update.getCallbackQuery().getFrom().getId();
            if (!checkUser(userId)) {
                System.out.println("NEW USER CQ");
                User user = new User();
                user.setFirstName(update.getCallbackQuery().getFrom().getFirstName());
                user.setLastName(update.getCallbackQuery().getFrom().getLastName());
                user.setUserName(update.getCallbackQuery().getFrom().getUserName());
                user.setTelegramId(userId);
                User u = userRepository.save(user);
                System.out.println("user id:" + u.getId() + " " + u.getTelegramId());
            }
            return callbackQueryHandler.processCallbackQuery(callbackQuery);
        }
        return null;
    }

    @SneakyThrows
    private BotApiMethod<?> handleInputMessage(Message message) {
        BotState botState;
        BotState msgHandlerState;
        String inputMsg = message.getText();
        String chatId = message.getChatId().toString();


        if (message.hasPhoto()) {
            System.out.println(">>>" + message);
            System.out.println(">>"+message.getPhoto().size());
            File file = telegramApiClient.getDocumentFile(message.getPhoto().get(3).getFileId(),"sas.png");
        }
        if (message.hasDocument()) {
            System.out.println(">>>" + message);
            System.out.println(">>"+message.getDocument().getFileId());
            File file = telegramApiClient.getDocumentFile(message.getDocument().getFileId(),message.getDocument().getFileId());
        }
        if(message.hasText()) {
            switch (inputMsg) {
                case "/start":
                    botState = BotState.START;
                    msgHandlerState = BotState.START;
                    break;
                case "/ex":
                    botState = BotState.EX;
                    msgHandlerState = BotState.EX;
                    execute(SendMessage.builder()
                            .chatId(chatId)
                            .text("https://images.pexels.com/photos/60597/dahlia-red-blossom-bloom-60597.jpeg?cs=srgb&dl=pexels-pixabay-60597.jpg&fm=jpg")
                            .replyMarkup(CallbackQueryHandler.getInlineMessageButtons(List.of("hi::hi")))
                            .build());
                    break;
                default:
                    botState = botStateCash.getBotStateMap().get(message.getFrom().getId()) == null ?
                            BotState.START : botStateCash.getBotStateMap().get(message.getFrom().getId());
                    msgHandlerState = messageHandlerCash.getBotStateMap().get(message.getFrom().getId()) == null ?
                            BotState.START : messageHandlerCash.getBotStateMap().get(message.getFrom().getId());
            }
            return messageHandler.handle(message, botState, msgHandlerState);
        }
        return null;
    }

    private SendMessage addUserDictionary(String chatId, String fileId) {
        try {
            addUserDictionary(chatId, telegramApiClient.getDocumentFile(fileId,"none.txt"));
            return new SendMessage(chatId, BotMessageEnum.SUCCESS_UPLOAD_MESSAGE.getMessage());
        } catch (TelegramFileNotFoundException e) {
            return new SendMessage(chatId, BotMessageEnum.EXCEPTION_TELEGRAM_API_MESSAGE.getMessage());
//        } catch (DictionaryTooBigException e) {
//            return new SendMessage(chatId, BotMessageEnum.EXCEPTION_TOO_LARGE_DICTIONARY_MESSAGE.getMessage());
        } catch (Exception e) {
            return new SendMessage(chatId, BotMessageEnum.EXCEPTION_BAD_FILE_MESSAGE.getMessage());
        }
    }

    public void addUserDictionary(String userId, File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
//            File file1 = new File("C:\\Users\\PC12\\IdeaProjects\\first\\src\\main\\resources\\");
//
//            copyInputStreamToFile(fileInputStream, file);
        }
    }

    @SneakyThrows
    public void sendPhoto(String chatId, String fileName) {
        File file = ResourceUtils.getFile(fileName);
        InputFile inputFile = new InputFile(file);
        SendPhoto sp = new SendPhoto();
        sp.setPhoto(inputFile);
        sp.setChatId(chatId);
        execute(sp);
    }

    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {

        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }
}
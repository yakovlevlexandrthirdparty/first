package ru.skb.rentguy.first.constants.bot;

/**
 * Текстовые сообщения, посылаемые ботом
 */
public enum BotMessageEnum {
    //ответы на команды с клавиатуры
    INPUT_DATE_MESSAGE("Введите предпологаемые даты заезда и выезда в формате:\nДД.ММ.ГГГГ-ДД.ММ.ГГГГ\n"),
    HELLO_MESSAGE("Добрый день, я бот RentGuy, \n\nЯ помогу вам с арендой квартиры в Сочи\n" +
            "❗ *Что Вы можете сделать:*\n" +
            "✅ Найти свободные объекты на нужные вам даты,\n" +
            "✅ Посмотреть фото и отзывы на данные объекты,\n" +
            "✅ Забронировать дату осмотра\n" +
            "Воспользуйтесь клавиатурой, чтобы начать работу"
    ),
    START_DATE_MESSAGE("Введите предполагаемую дату заезда в формате ДД.ММ.ГГГГ например 02.06.2022"
    ),
    END_DATE_MESSAGE("Введите предполагаемую выезда в формате ДД.ММ.ГГГГ например 02.06.2022"
    ),
    ACCEPT_DATES("Подтвердите введенные даты:\n"
    ),
    HELP_MESSAGE("\uD83D\uDC4B Привет, я бот ПишиЧитай, и я помогу Вам создать задания на правописание словарных " +
            "слов для Ваших детей\n\n" +
            "❗ *Что Вы можете сделать:*\n" +
            "✅ скачать Word-файл с заданиями, составленными из списков словарных слов за 1-4 классы (по отдельности " +
            "или сразу всех вместе)\n" +
            "✅ изменить любой из этих словарей и загрузить его как свой - скачайте нужный словарь в " +
            "Excel-файл, внесите в него изменения и отправьте мне\n" +
            "✅ создать свой словарь с нуля - скачайте шаблон, заполните его и отправьте мне " +
            "(максимальный размер - 1 000 слов)\n\n" +
            "В мои словари уже добавлены все слова из программ \"Школа России\" и \"Начальная школа XXI века\", " +
            "но если в списке Вашего ребёнка есть другие слова, присылайте их моему создателю @taksebe\n\n" +
            "Обратите внимание, что некоторые слова попадают в словари сразу нескольких классов - это следствие " +
            "использования списков из разных программ и дополнений от пользователей. Это не страшно, " +
            "ведь повторение - мать учения\n\n" +
            "Удачи!\n\n" +
            "Воспользуйтесь клавиатурой, чтобы начать работу\uD83D\uDC47"),
    CHOSE_ITEM("Аренда автомобилей и квартир в Сочи.\n\n Выбирайте и бранируйте машины и квартиры в одном приложении.\n\n Выбирите услугу\uD83D\uDC47"),
    CHOOSE_DICTIONARY_MESSAGE("Выберите словарь\uD83D\uDC47 "),
    UPLOAD_DICTIONARY_MESSAGE("Добавьте файл, соответствующий шаблону. Вы можете сделать это в любой момент"),
    NON_COMMAND_MESSAGE("Пожалуйста, воспользуйтесь клавиатурой\uD83D\uDC47"),

    //результаты загрузки словаря
    SUCCESS_UPLOAD_MESSAGE("\uD83D\uDC4D Словарь успешно загружен"),
    EXCEPTION_TELEGRAM_API_MESSAGE("Ошибка при попытку получить файл из API Telegram"),
    EXCEPTION_TOO_LARGE_DICTIONARY_MESSAGE("В словаре больше 1 000 слов. Едва ли такой большой набор словарных " +
            "слов действительно нужен, ведь я работаю для обучения детей"),
    EXCEPTION_BAD_FILE_MESSAGE("Файл не может быть обработан. Вы шлёте мне что-то не то, балуетесь, наверное"),

    //ошибки при обработке callback-ов
    EXCEPTION_BAD_BUTTON_NAME_MESSAGE("Неверное значение кнопки. Крайне странно. Попробуйте позже"),
    EXCEPTION_DICTIONARY_NOT_FOUND_MESSAGE("Словарь не найден"),
    EXCEPTION_DICTIONARY_WTF_MESSAGE("Нежиданная ошибка при попытке получить словарь. Сам в шоке"),
    EXCEPTION_TASKS_WTF_MESSAGE("Нежиданная ошибка при попытке получить задания. Сам в шоке"),
    EXCEPTION_TEMPLATE_WTF_MESSAGE("Нежиданная ошибка при попытке получить шаблон. Сам в шоке"),

    //прочие ошибки
    EXCEPTION_ILLEGAL_MESSAGE("!"),
    EXCEPTION_WHAT_THE_FUCK("Что-то пошло не так. Обратитесь к программисту");

    private final String message;

    BotMessageEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
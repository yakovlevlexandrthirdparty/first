package ru.skb.rentguy.first.telegram;

import ru.skb.rentguy.first.entities.Order;

public class BtnUtils {
    public static String paidOrder(Order order) {
        if (order.getState()==1) {
            return "✅";
        } else {
            return "";
        }
    }

    public static String daysNaming(int value) {
        switch (value) {
            case 1:
                return "1 день";
            case 2:
                return "2 дня";
            case 3:
                return "3 дня";
            case 4:
                return "4 дня";
            default:
                return value+" дней";
        }
    }

    public static String orderMessagePostfix(Order order){
        if(order.getState()==0){
            return " \n\nКак получите оплату нажмите на кнопку \"Оплачено\" и даты исчезнут из списка доступных\uD83D\uDC47\uD83C\uDFFD";
        } else {
            return " \n\n✅ Заказ оплачен ✅";
        }
    }
}

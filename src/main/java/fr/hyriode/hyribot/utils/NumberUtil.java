package fr.hyriode.hyribot.utils;

public class NumberUtil {
    public static boolean isNumber(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

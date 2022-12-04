package fr.hyriode.hyribot.utils;

public class TimeUtil {

    public static long getTimeStringToMillis(String time) {
        long result = 0;
        String[] split = time.split(" ");
        for (String s : split) {
            if (s.contains("s")) {
                result += Long.parseLong(s.replace("s", "")) * 1000;
            } else if (s.contains("m")) {
                result += Long.parseLong(s.replace("m", "")) * 1000 * 60;
            } else if (s.contains("h")) {
                result += Long.parseLong(s.replace("h", "")) * 1000 * 60 * 60;
            } else if (s.contains("d")) {
                result += Long.parseLong(s.replace("d", "")) * 1000 * 60 * 60 * 24;
            }
        }
        return result;
    }

}

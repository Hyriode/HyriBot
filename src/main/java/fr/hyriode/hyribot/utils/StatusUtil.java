package fr.hyriode.hyribot.utils;

import net.dv8tion.jda.api.OnlineStatus;

public class StatusUtil {

    public static String getStatusToString(OnlineStatus onlineStatus){
        String result = "Hors ligne ";
        switch (onlineStatus){
            case ONLINE:
                result = "En ligne ";
                break;
            case IDLE:
                result = "Absent ";
                break;
            case DO_NOT_DISTURB:
                result = "Ne pas déranger ";
                break;
        }
        return result + getStatusIconToString(onlineStatus);
    }

    public static String getStatusIconToString(OnlineStatus onlineStatus){
        switch (onlineStatus){
            case ONLINE:
                return "🟢";
            case IDLE:
                return "🌙";
            case DO_NOT_DISTURB:
                return "⛔";
            default:
                return "⚫";
        }

    }

    public static String getStatusVoiceChannel(boolean aPublic) {
        return aPublic ? "🔓" : "🔒";
    }
}

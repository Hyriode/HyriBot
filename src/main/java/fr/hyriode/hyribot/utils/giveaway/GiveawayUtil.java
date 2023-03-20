package fr.hyriode.hyribot.utils.giveaway;

import fr.hyriode.hyribot.giveaway.Giveaway;

public class GiveawayUtil {

    public static String getStatus(Giveaway giveaway) {
        if (giveaway.isStopped()) {
            return "🔴";
        }
        return "🟢";
    }
}

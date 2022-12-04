package fr.hyriode.hyribot.utils.giveaway;

import fr.hyriode.hyribot.giveaway.Giveaway;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import javafx.beans.property.adapter.JavaBeanBooleanPropertyBuilder;
import net.dv8tion.jda.api.EmbedBuilder;

public class GiveawayUtil {

    public static String getStatus(Giveaway giveaway) {
        if (giveaway.isStopped()) {
            return "🔴";
        }
        return "🟢";
    }
}

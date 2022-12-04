package fr.hyriode.hyribot.listener.model.giveaway;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.giveaway.Giveaway;
import fr.hyriode.hyribot.giveaway.GiveawayManager;
import fr.hyriode.hyribot.listener.HyriListener;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.jetbrains.annotations.NotNull;

public class GiveawayListener extends HyriListener {
    public GiveawayListener(HyriBot bot) {
        super(bot);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        GiveawayManager giveawayManager = this.bot.getGiveawayManager();

        for (Giveaway giveaway : giveawayManager.getGiveaways(giveaway -> !giveaway.isStopped())) {
            giveawayManager.startGiveaway(giveaway);
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if(event.getUser().isBot()) return;

        if(event.getEmoji().getAsReactionCode().equals("🎉")) {
            GiveawayManager giveawayManager = this.bot.getGiveawayManager();
            Giveaway giveaway = giveawayManager.getGiveawayByMessageId(event.getMessageIdLong());

            if(giveaway != null && !giveaway.isStopped() && !giveaway.isEnded()) {
                giveaway.addParticipant(event.getUserIdLong());
                giveaway.update();
            }
        }

    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if(event.getUser().isBot()) return;

        if(event.getEmoji().getAsReactionCode().equals("🎉")) {
            GiveawayManager giveawayManager = this.bot.getGiveawayManager();
            Giveaway giveaway = giveawayManager.getGiveawayByMessageId(event.getMessageIdLong());

            if(giveaway != null && !giveaway.isStopped() && !giveaway.isEnded()) {
                giveaway.removeParticipant(event.getUserIdLong());
                giveaway.update();
            }
        }
    }
}

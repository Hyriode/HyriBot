package fr.hyriode.hyribot.listener.model.log;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.listener.HyriListener;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import org.jetbrains.annotations.NotNull;

public class LogListener extends HyriListener {

    public LogListener(HyriBot bot) {
        super(bot);
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        super.onMessageDelete(event);
    }

    @Override
    public void onMessageBulkDelete(@NotNull MessageBulkDeleteEvent event) {
        super.onMessageBulkDelete(event);
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        super.onMessageUpdate(event);
    }

    @Override
    public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {
    }
}

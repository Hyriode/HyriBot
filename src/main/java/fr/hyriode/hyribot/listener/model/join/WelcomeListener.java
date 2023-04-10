package fr.hyriode.hyribot.listener.model.join;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.listener.HyriListener;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.jetbrains.annotations.NotNull;

public class WelcomeListener extends HyriListener {
    public WelcomeListener(HyriBot bot) {
        super(bot);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        TextChannel textChannel = event.getGuild().getTextChannelById(this.bot.getConfig().getWelcomeChannel());

        if(textChannel != null) {
            textChannel.sendMessage("Bienvenue " + event.getUser().getAsMention() + " sur " + event.getGuild().getName() + " !").queue();
        }
    }

}

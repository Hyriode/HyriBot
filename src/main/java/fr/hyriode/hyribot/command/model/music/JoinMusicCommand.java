package fr.hyriode.hyribot.command.model.music;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class JoinMusicCommand extends HyriSlashCommand {

    public JoinMusicCommand(HyriBot bot) {
        super(bot, "join", "Rejoindre le salon vocal.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();

        if(member != null) {
            boolean canJoin = this.bot.getMusicManager().joinChannel(member);

            if(!canJoin) {
                event.reply("Je ne peux pas rejoindre le salon vocal.").queue();
                return;
            }

            event.reply("Je suis connecté au salon vocal.").queue();
        }
    }

}

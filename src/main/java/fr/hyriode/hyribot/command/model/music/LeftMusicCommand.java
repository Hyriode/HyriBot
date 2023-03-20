package fr.hyriode.hyribot.command.model.music;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LeftMusicCommand extends HyriSlashCommand {

    public LeftMusicCommand(HyriBot bot) {
        super(bot, "left", "Quitter le salon vocal.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        boolean isLeft = this.bot.getMusicManager().leaveChannel(event.getMember());

        if (isLeft) {
            event.reply("Je suis déconnecté du salon vocal.").queue();
            return;
        }

        event.reply("Je ne peux pas quitter le salon vocal.").queue();
    }
}

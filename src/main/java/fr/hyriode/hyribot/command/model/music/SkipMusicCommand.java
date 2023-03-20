package fr.hyriode.hyribot.command.model.music;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SkipMusicCommand extends HyriSlashCommand {
    public SkipMusicCommand(HyriBot bot) {
        super(bot, "skip", "Passer la musique.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        boolean isSkipped = this.bot.getMusicManager().skipTrack(event.getMember());

        if (isSkipped) {
            event.reply("La musique a été passée.").queue();
            return;
        }

        event.reply("Je ne peux pas passer la musique.").queue();
    }
}

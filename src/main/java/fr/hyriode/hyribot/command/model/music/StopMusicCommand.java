package fr.hyriode.hyribot.command.model.music;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StopMusicCommand extends HyriSlashCommand {

    public StopMusicCommand(HyriBot bot) {
        super(bot, "stop", "Arrêter la musique.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        boolean isStopping = this.bot.getMusicManager().stop(event.getMember());

        if (isStopping) {
            event.reply("La musique a été arrêtée.").queue();
            return;
        }

        event.reply("Je ne peux pas arrêter la musique.").queue();
    }
}

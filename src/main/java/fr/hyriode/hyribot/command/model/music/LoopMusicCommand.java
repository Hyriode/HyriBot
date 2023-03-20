package fr.hyriode.hyribot.command.model.music;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LoopMusicCommand extends HyriSlashCommand {

    public LoopMusicCommand(HyriBot bot) {
        super(bot, "loop", "Mettre en boucle les musiques en cours de lecture.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if(this.bot.getMusicManager().loop(event.getGuild())) {
            event.reply("La musique a été mise en boucle.").queue();
            return;
        }
        event.reply("La musique n'a pas pu être mise en boucle.").queue();
    }


}

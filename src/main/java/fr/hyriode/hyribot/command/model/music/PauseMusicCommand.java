package fr.hyriode.hyribot.command.model.music;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PauseMusicCommand extends HyriSlashCommand {

    public PauseMusicCommand(HyriBot bot) {
        super(bot, "pause", "Mettre en pause la musique en cours de lecture.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if(member != null && this.bot.getMusicManager().pause(member)) {
            event.reply("La musique a été mise en pause.").queue();
            return;
        }
        event.reply("La musique n'a pas pu être mise en pause car vous n'êtes pas dans le meme channel que le bot").queue();
    }
}

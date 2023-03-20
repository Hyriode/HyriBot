package fr.hyriode.hyribot.command.model.music;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.music.MusicManager;
import fr.hyriode.hyribot.music.ServiceMusic;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public class PlayMusicCommand extends HyriSlashCommand {
    public PlayMusicCommand(HyriBot bot) {
        super(bot, new CommandDataImpl("play", "Jouer une musique.")
                .addOption(OptionType.STRING, "search", "L'url ou un mot clé (recherche sur YouTube par défaut) de la musique à jouer.", true)
                .addOptions(new OptionData(OptionType.STRING, "service" , "Rechercher sur un service de musique spécifique", false)
                        .addChoices(ServiceMusic.getAsChoices())));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String url = event.getOption("search").getAsString();
        OptionMapping optionService = event.getOption("service");
        ServiceMusic serviceMusic = optionService != null
                ? ServiceMusic.valueOf(optionService.getAsString()) : ServiceMusic.YOUTUBE;

        MusicManager musicManager = this.bot.getMusicManager();
        Member member = event.getMember();
        InteractionHook ih = event.reply("Merci de patienter...").complete();

        musicManager.play(member, ih, serviceMusic, url);
    }
}

package fr.hyriode.hyribot.command.model;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class InfoCommand extends HyriSlashCommand {

    public InfoCommand(HyriBot bot) {
        super(bot, "info", "Affiche les informations du serveur.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if(guild == null) return;

        String guildIconURL = guild.getIconUrl();
        String guildBannerURL = guild.getBannerUrl();

        StringBuilder desc = new StringBuilder();
        EmbedBuilder e = new HyriEmbedBuilder()
                .setTitle("Informations");

        if(guildIconURL != null) e.setThumbnail(guildIconURL);
        if(guildBannerURL != null) e.setImage(guildBannerURL + "?size=256");

        desc.append("Minecraft Java (Non Bedrock)\n");
        desc.append("Type: Mini-Jeux\n");
        desc.append("Version: 1.8-1.19\n");
        desc.append("Ouvert à tout le monde\n");

        event.replyEmbeds(e.setDescription(desc).build()).queue();
    }
}

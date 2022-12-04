package fr.hyriode.hyribot.command.model;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.network.IHyriNetwork;
import fr.hyriode.hyribot.Bootstrap;
import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class StatsCommand extends HyriSlashCommand {
    public StatsCommand(HyriBot bot) {
        super(bot);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if(guild == null) return;

        EmbedBuilder e = new HyriEmbedBuilder();
        e.setTitle("Stats du serveur");
        e.setThumbnail(guild.getIconUrl());
        e.addField("Membres en ligne",
                "Discord : " + (int) guild.getMembers().stream().filter(member -> member.getOnlineStatus() != OnlineStatus.OFFLINE && !member.getUser().isBot()).count() +
                        "/" + guild.getMembers().stream().filter(member -> !member.getUser().isBot()).count() + "\n" +
                        "En Jeu : " + getStatsServer(), true);
        event.replyEmbeds(e.build()).queue();
    }

    private String getStatsServer() {
        IHyriNetwork network = HyriAPI.get().getNetworkManager().getNetwork();
        if(network != null) {
            return network.getPlayerCounter().getPlayers() + "/" + network.getSlots();
        }
        return "(Soon)";
    }

    @Override
    public SlashCommandData getData() {
        return null;
    }
}

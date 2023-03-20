package fr.hyriode.hyribot.command.model;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.network.IHyriNetwork;
import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StatsCommand extends HyriSlashCommand {
    public StatsCommand(HyriBot bot) {
        super(bot, "stats", "Affiche les stats du serveur");
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
                        "En Jeu : " + this.getStatsServer(), true);
        event.replyEmbeds(e.build()).queue();
    }

    private String getStatsServer() {
        IHyriNetwork network = HyriAPI.get().getNetworkManager().getNetwork();
        if(network != null && network.getSlots() > -1) {
            return network.getPlayerCounter().getPlayers() + "/" + network.getSlots();
        }
        return "Maintenance";
    }

}

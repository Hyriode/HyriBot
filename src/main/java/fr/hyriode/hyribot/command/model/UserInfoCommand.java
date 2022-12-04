package fr.hyriode.hyribot.command.model;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import fr.hyriode.hyribot.utils.StatusUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

public class UserInfoCommand extends HyriSlashCommand {
    public UserInfoCommand(HyriBot bot) {
        super(bot);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping optionMember = event.getOption("user");
        Member member = optionMember != null ? optionMember.getAsMember() : event.getMember();
        StringBuilder roles = new StringBuilder();

        if(member == null) {
            event.reply("Impossible de trouver l'utilisateur").queue();
            return;
        }

        String memberAvatarURL = member.getAvatarUrl() != null
                ? member.getAvatarUrl()
                : member.getUser().getAvatarUrl() != null
                ? member.getUser().getAvatarUrl()
                : member.getUser().getDefaultAvatarUrl();

        member.getRoles().stream().map(IMentionable::getAsMention).forEach(role -> roles.append(role + " "));

        EmbedBuilder e = new HyriEmbedBuilder();
        e.setTitle(member.getUser().getAsTag() + " - " + StatusUtil.getStatusToString(member.getOnlineStatus()));
        e.setThumbnail(member.getUser().getAvatarUrl());
        e.addField("Dates",
                "De création : <t:" + member.getTimeCreated().toEpochSecond() + ">\n" +
                "D'arrivée : <t:" + member.getTimeJoined().toEpochSecond() + ">",
                false);
        e.addField("Roles", roles.toString(), false);
        e.setFooter("ID : " + member.getId());

        event.replyEmbeds(e.build()).queue();
    }

    @Override
    public SlashCommandData getData() {
        return new CommandDataImpl("userinfo", "Affiche les informations d'un utilisateur")
                .addOption(OptionType.USER, "user", "L'utilisateur à afficher", false);
    }
}

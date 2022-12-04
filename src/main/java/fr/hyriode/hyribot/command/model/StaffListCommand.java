package fr.hyriode.hyribot.command.model;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.command.HyriodeRole;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import fr.hyriode.hyribot.utils.StatusUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StaffListCommand extends HyriSlashCommand {

    public StaffListCommand(HyriBot bot) {
        super(bot);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if(guild == null) return;

        final EmbedBuilder e = new HyriEmbedBuilder();
        final HashMap<Role, List<Member>> roles = new HashMap<>();

        for (Role role : guild.getRoles()) {
            if(role.getIdLong() == HyriodeRole.STAFF.getRoleId() || role.getIdLong() == HyriodeRole.PLAYER.getRoleId()) continue;
            final List<Member> members = new ArrayList<>();
            guild.getMembers().forEach(member -> {
                if (member.getRoles().contains(role) && member.getRoles().stream().anyMatch(role1 -> role1.getIdLong() == HyriodeRole.STAFF.getRoleId())) {
                    if(roles.values().stream().noneMatch(members1 -> members1.contains(member)))
                        members.add(member);
                }
            });
            if(members.size() > 0)
                roles.put(role, members);
        }

        roles.forEach((role, members) -> {
            StringBuilder membersS = new StringBuilder();
            members.forEach(member -> {
                membersS.append(StatusUtil.getStatusIconToString(member.getOnlineStatus()) + " " + member.getAsMention()).append("\n");
            });
            e.addField(role.getName(), membersS.toString(), true);
        });

        e.setTitle("Staff d'Hyriode");

        event.replyEmbeds(e.build()).queue();
    }

    @Override
    public SlashCommandData getData() {
        return new CommandDataImpl("staff", "Affiche la liste du staff d'Hyriode.");
    }
}

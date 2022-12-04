package fr.hyriode.hyribot.ticket;

import fr.hyriode.hyribot.Bootstrap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TicketProgress {

    private final String id;
    private final long requesterId;
    private final long guildId;
    private final long channelId;
    private final TicketType type;

    public TicketProgress(String id, long requesterId, long guildId, long channelId, TicketType type) {
        this.id = id;
        this.requesterId = requesterId;
        this.guildId = guildId;
        this.channelId = channelId;
        this.type = type;
    }

    public String getId() {
        return this.id;
    }

    public long getRequesterId() {
        return this.requesterId;
    }

    public long getGuildId() {
        return this.guildId;
    }

    public long getChannelId() {
        return this.channelId;
    }

    public TicketType getType() {
        return this.type;
    }

    public void addMember(Member member) {
        if(member == null) return;

        Guild guild = member.getGuild();
        TextChannel channel = guild.getTextChannelById(this.channelId);

        if(channel == null) return;

        channel.getManager().putMemberPermissionOverride(
                member.getIdLong(),
                EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL),
                null
        ).queue();
    }

    public void removeMember(Member member) {
        if(member == null) return;

        Guild guild = member.getGuild();
        TextChannel channel = guild.getTextChannelById(this.channelId);

        if(channel == null) return;

        channel.getManager().removePermissionOverride(member.getIdLong()).queue();
    }

    public List<Member> getMembers(Guild guild, Predicate<Member> filter) {
        TextChannel channel = guild.getTextChannelById(this.channelId);
        if(channel == null) return null;
        return channel.getMemberPermissionOverrides().stream()
                .map(PermissionOverride::getMember)
                .filter(filter)
                .collect(Collectors.toList());
    }

    public void close(Member closer, List<Message> messages) {
        List<MessageTicket> messageTickets = messages.stream().map(MessageTicket::new).collect(Collectors.toList());
        Bootstrap.getCurrentBot().getTicketManager().closeTicket(
                new TicketClosed(
                        this.id,
                        this.requesterId,
                        this.guildId,
                        this.channelId,
                        this.type,
                        true,
                        System.currentTimeMillis(),
                        closer.getIdLong(),
                        messageTickets
                )
        );
    }

    public void update() {
        Bootstrap.getCurrentBot().getTicketManager().update(this);
    }

}

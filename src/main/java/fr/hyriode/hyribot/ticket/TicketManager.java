package fr.hyriode.hyribot.ticket;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriodeRole;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import java.util.*;
import java.util.stream.Collectors;

public class TicketManager {

    private static final String REDIS_TICKETS_PROGRESS = HyriBot.NAME_KEY + ":ticketsProgress";
    private static final String REDIS_TICKETS_CLOSED = HyriBot.NAME_KEY + ":ticketsClosed";

    private final List<TicketProgress> ticketsProgress;
    private final List<TicketClosed> ticketsClosed;

    private final HyriBot bot;

    public TicketManager(HyriBot bot) {
        this.bot = bot;
        this.ticketsProgress = HyriAPI.get().getRedisProcessor().get(jedis -> jedis.hgetAll(REDIS_TICKETS_PROGRESS).values().stream().map(s -> HyriAPI.GSON.fromJson(s, TicketProgress.class)).collect(Collectors.toList()));
        this.ticketsClosed = HyriAPI.get().getRedisProcessor().get(jedis -> jedis.hgetAll(REDIS_TICKETS_CLOSED).values().stream().map(s -> HyriAPI.GSON.fromJson(s, TicketClosed.class)).collect(Collectors.toList()));
    }

    public TicketProgress createTicket(Member requester, TicketReportType reportType, TicketType ticketType) {
        Guild guild = requester.getGuild();
        Category categoryTicket = guild.getCategoryById(reportType.getCategoryId());
        if(categoryTicket == null) return null;

        TextChannel ticketChannel = this.createTicketChannel(requester, categoryTicket, ticketType);
        if(ticketChannel == null) return null;

        ticketChannel.sendMessage(requester.getAsMention()).setEmbeds(this.getTicketEmbed(requester))
                .setActionRow(Button.secondary("ticket.close", "Fermer le Ticket"),
                        Button.success("ticket.add_member", "Ajouter un membre"),
                        Button.danger("ticket.remove_member", "Retirer un membre")).queue();

        TicketProgress ticket = new TicketProgress(UUID.randomUUID().toString(), requester.getIdLong(), guild.getIdLong(), ticketChannel.getIdLong(), ticketType);

        this.ticketsProgress.add(ticket);
        this.update(ticket);

        return ticket;
    }

    private MessageEmbed getTicketEmbed(Member member) {
        return new HyriEmbedBuilder()
                .setTitle("Ticket - " + member.getEffectiveName())
                .setDescription("Pour fermer votre ticket, veuillez cliquer sur le bouton ci-dessous.")
                .build();
    }

    private TextChannel createTicketChannel(Member requester, Category categoryTicket, TicketType ticketType) {
        Guild guild = requester.getGuild();
        Role mod = guild.getRoleById(HyriodeRole.MODERATOR.getRoleId());
        if(mod == null) return null;

        ChannelAction<TextChannel> channelAction = categoryTicket.createTextChannel("ticket-" + requester.getUser().getName())
                .addPermissionOverride(requester, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND));

        if(ticketType == TicketType.ADMIN)
            return channelAction
                    .addPermissionOverride(mod, EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                    .complete();

        return channelAction.complete();
    }

    public void update(TicketProgress ticket) {
        HyriAPI.get().getRedisProcessor().process(jedis -> jedis.hset(REDIS_TICKETS_PROGRESS, ticket.getId(), HyriAPI.GSON.toJson(ticket)));
    }

    private void removeProgressTicket(String id) {
        HyriAPI.get().getRedisProcessor().process(jedis -> jedis.hdel(REDIS_TICKETS_PROGRESS, id));
        this.ticketsProgress.removeIf(ticketProgress -> ticketProgress.getId().equals(id));
    }

    private void addClosedTicket(TicketClosed ticketClosed) {
        HyriAPI.get().getRedisProcessor().process(jedis -> jedis.hset(REDIS_TICKETS_CLOSED, ticketClosed.getId(), HyriAPI.GSON.toJson(ticketClosed)));
        this.ticketsClosed.add(ticketClosed);
    }

    public void closeTicket(TicketClosed ticketClosed) {
        this.removeProgressTicket(ticketClosed.getId());
        this.addClosedTicket(ticketClosed);
    }

    public List<TicketProgress> getTicketsProgress() {
        return ticketsProgress;
    }

    public List<TicketClosed> getTicketsClosed() {
        return ticketsClosed;
    }

    public TicketProgress getTicketProgress(long idLong) {
        return this.ticketsProgress.stream().filter(ticketProgress -> ticketProgress.getChannelId() == idLong).findFirst().orElse(null);
    }
}

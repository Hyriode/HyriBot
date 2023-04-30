package fr.hyriode.hyribot.ticket;

import java.util.List;

public class TicketClosed {

    private final String id;
    private final long requesterId;
    private final long guildId;
    private final long channelId;

    private final boolean closed;
    private final long closedAt;
    private final long closerId;
    private final List<MessageTicket> messagesTicket;

    public TicketClosed(String id, long requesterId, long guildId, long channelId, boolean closed, long closedAt, long closerId, List<MessageTicket> messagesTicket) {
        this.id = id;
        this.requesterId = requesterId;
        this.guildId = guildId;
        this.channelId = channelId;
        this.closed = closed;
        this.closedAt = closedAt;
        this.closerId = closerId;
        this.messagesTicket = messagesTicket;
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

    public boolean isClosed() {
        return this.closed;
    }

    public long getClosedAt() {
        return this.closedAt;
    }

    public long getCloserId() {
        return this.closerId;
    }

    public List<MessageTicket> getMessagesTicket() {
        return this.messagesTicket;
    }
}

package fr.hyriode.hyribot.giveaway;

import fr.hyriode.hyribot.Bootstrap;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;

import java.util.ArrayList;
import java.util.List;

public class Giveaway {

    private final String id;

    private final long hosterId;
    private final long guildId;
    private final long channelId;
    private long messageId;

    private final String prize;
    private final long winners;
    private final long duration;
    private final long startedAt;

    private boolean stopped = false;

    private final List<Long> participants = new ArrayList<>();
    private final List<Long> winnersParticipants = new ArrayList<>();

    public Giveaway(String id, long hosterId, long guildId, long channelId, String prize, long winners, long duration, long startedAt) {
        this.id = id;
        this.hosterId = hosterId;
        this.guildId = guildId;
        this.channelId = channelId;
        this.prize = prize;
        this.winners = winners;
        this.duration = duration;
        this.startedAt = startedAt;
    }

    public String getId() {
        return id;
    }

    public long getHosterId() {
        return hosterId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setMessageId(long idLong) {
        this.messageId = idLong;
    }

    public long getMessageId() {
        return messageId;
    }

    public String getPrize() {
        return prize;
    }

    public long getWinners() {
        return winners;
    }

    public long getDuration() {
        return duration;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public List<Long> getParticipants() {
        return participants;
    }

    public List<Long> getWinnersParticipants() {
        return winnersParticipants;
    }

    public void addWinner(long winnerId) {
        this.winnersParticipants.add(winnerId);
    }

    public void addParticipant(long userId) {
        this.participants.add(userId);
    }

    public void removeParticipant(long userId) {
        this.participants.remove(userId);
    }

    public boolean isParticipant(long userId) {
        return this.participants.contains(userId);
    }

    public boolean isEnded() {
        return System.currentTimeMillis() >= this.startedAt + this.duration;
    }

    public EmbedBuilder asEmbedResult() {
        EmbedBuilder em = new HyriEmbedBuilder();
        em.setTitle("Giveaway • " + this.getPrize());
        em.setDescription("Le giveaway est terminé gros, le(s) gagnant est : " + this.getWinner());

        return em;
    }

    public EmbedBuilder asEmbedCreate() {
        EmbedBuilder em = new HyriEmbedBuilder();
        em.setTitle("Giveaway • " + this.getPrize());
        em.setDescription("Nombre de gagnants: " + this.getWinners() + "\n" +
                "<t:" + ((this.getStartedAt() + this.getDuration()) / 1000) + ":R>");
        System.out.println(em.getDescriptionBuilder());

        return em;
    }

    public EmbedBuilder asEmbedError() {
        return new HyriEmbedBuilder()
                .setTitle("Giveaway • " + this.getPrize())
                .setDescription("Le giveaway est terminé gros, mais il n'y a pas eu de gagnant");
    }

    public String getWinner() {
        StringBuilder builder = new StringBuilder();
        for (long winner : this.winnersParticipants) {
            builder.append("<@").append(winner).append("> ");
        }
        return builder.toString();
    }

    public void update() {
        Bootstrap.getCurrentBot().getGiveawayManager().update(this);
    }
}

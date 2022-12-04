package fr.hyriode.hyribot.giveaway;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.utils.ReaderConsole;
import fr.hyriode.hyribot.utils.ThreadUtil;
import fr.hyriode.hyribot.utils.giveaway.GiveawayUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GiveawayManager {

    private static final String REDIS_KEY = HyriBot.NAME_KEY + ":giveaways";

    private final HyriBot bot;

    private final List<Giveaway> giveaways;

    public GiveawayManager(HyriBot bot) {
        this.bot = bot;
        this.giveaways = new ArrayList<>();
        this.giveaways.addAll(this.getGiveawaysAPI());
    }

    public void createGiveaway(long hosterId, long guildId, long channelId, String prize, long winners, long duration, long startedAt) {
        Giveaway giveaway = new Giveaway(UUID.randomUUID().toString(), hosterId, guildId, channelId, prize, winners, duration, startedAt);
        this.giveaways.add(giveaway);
        this.startGiveaway(giveaway);

        TextChannel textChannel = this.bot.getJDA().getTextChannelById(channelId);

        if(textChannel != null) {
            textChannel.sendMessageEmbeds(giveaway.asEmbedCreate().build()).queue(message -> {
                giveaway.setMessageId(message.getIdLong());
                message.addReaction(Emoji.fromUnicode("🎉")).queue();
            });
        }

        giveaway.update();
    }

    public void startGiveaway(Giveaway giveaway) {
        Guild guild = this.bot.getJDA().getGuildById(giveaway.getGuildId());
        if(giveaway.isStopped() || guild == null) return;

        Thread thread = new Thread(() -> {
            while(!giveaway.isEnded() && !giveaway.isStopped()) {
                ThreadUtil.sleep(1000);
            }
            TextChannel textChannel = guild.getTextChannelById(giveaway.getChannelId());
            this.stopGiveaway(giveaway);
            boolean winSuccess = this.winGiveaway(giveaway);
            if(textChannel != null) {
                if(winSuccess) {
                    textChannel.editMessageEmbedsById(giveaway.getMessageId(), giveaway.asEmbedResult().build()).queue();
                    return;
                }
                textChannel.editMessageEmbedsById(giveaway.getMessageId(), giveaway.asEmbedError().build()).queue();
            }
        });

        thread.setDaemon(false);
        thread.start();
    }

    private boolean winGiveaway(Giveaway giveaway) {
        List<Long> participants = new ArrayList<>(giveaway.getParticipants());
        if(participants.isEmpty()) {
            return false;
        }
        for (int i = 0; i < giveaway.getWinners(); i++) {
            Long[] participantsArray = participants.toArray(new Long[0]);
            long participant = participantsArray[new Random().nextInt(participantsArray.length)];
            participants.remove(participant);
            giveaway.addWinner(participant);
        }
        giveaway.update();
        return true;
    }

    public void stopGiveaway(Giveaway giveaway) {
        giveaway.setStopped(true);
        giveaway.update();
    }

    public void stopGiveaway(String id) {
        this.giveaways.stream().filter(giveaway -> giveaway.getId().equals(id)).findFirst().ifPresent(this::stopGiveaway);
    }

    public List<Giveaway> getGiveaways() {
        return giveaways;
    }

    public Giveaway getGiveaway(long messageId) {
        return this.giveaways.stream().filter(giveaway -> giveaway.getMessageId() == messageId).findFirst().orElse(null);
    }

    public Giveaway getGiveawayByMessageId(long messageId) {
        return this.giveaways.stream().filter(giveaway -> giveaway.getMessageId() == messageId).findFirst().orElse(null);
    }

    public List<Giveaway> getGiveaways(Predicate<Giveaway> filter) {
        return this.giveaways.stream().filter(filter).collect(Collectors.toList());
    }

    public List<Giveaway> getGiveawaysAPI() {
        return ((HashMap<?, ?>) HyriAPI.get().getRedisProcessor().get(jedis -> jedis.hgetAll(REDIS_KEY))).values()
                .stream().map(o -> HyriAPI.GSON.fromJson(o.toString(), Giveaway.class)).collect(Collectors.toList());
    }

    public void update(Giveaway giveaway) {
        HyriAPI.get().getRedisProcessor().process(jedis -> jedis.hset(REDIS_KEY, giveaway.getId(), HyriAPI.GSON.toJson(giveaway)));
    }
}

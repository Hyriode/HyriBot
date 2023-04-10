package fr.hyriode.hyribot.configuration;

import fr.hyriode.api.config.MongoDBConfig;
import fr.hyriode.api.config.RedisConfig;
import fr.hyriode.hyribot.command.HyriodeRole;
import fr.hyriode.hyribot.idea.IdeaType;
import fr.hyriode.hyribot.ticket.TicketReportType;

import java.util.HashMap;
import java.util.Map;

public class HyriConfig {

    private final RedisConfig redis;
    private final MongoDBConfig mongoDB;

    private final String token;
    private final Map<String, Long> roles;
    private final Map<String, Long> categoryTickets;
    private final Map<String, Long> ideaChannels;
    private final Map<String, Long> voiceCustom;
    private final Long statusChannel;
    private final Long guildId;
    private final Long reportChannel;
    private final Long welcomeChannel;

    public HyriConfig(RedisConfig redisConfig, MongoDBConfig mongoDBConfig) {
        this.redis = redisConfig;
        this.mongoDB = mongoDBConfig;
        this.token = System.getenv("TOKEN_HYRIBOT");
        this.roles = new HashMap<>();
        this.categoryTickets = new HashMap<>();
        this.ideaChannels = new HashMap<>();
        this.voiceCustom = new HashMap<>();
        this.statusChannel = null;
        this.guildId = null;
        this.reportChannel = null;
        this.welcomeChannel = null;
    }

    public String getToken() {
        return this.token;
    }

    public Long getRoleId(HyriodeRole hyriodeRole) {
        return this.roles.get(hyriodeRole.getName());
    }

    public Map<String, Long> getRoles() {
        return this.roles;
    }

    public RedisConfig getRedis() {
        return this.redis;
    }

    public MongoDBConfig getMongoDB() {
        return this.mongoDB;
    }

    public Map<String, Long> getCategoryTickets() {
        return categoryTickets;
    }

    public long getCategoryTicketId(TicketReportType ticketReportType) {
        return this.categoryTickets.get(ticketReportType.name().toLowerCase());
    }

    public long getIdeaChannelId(IdeaType type) {
        return this.ideaChannels.get(type.getName());
    }

    public Map<String, Long> getIdeaChannels() {
        return ideaChannels;
    }

    public long getVoiceCustomCategory() {
        return this.voiceCustom.get("category");
    }

    public long getVoiceCustomChannel() {
        return this.voiceCustom.get("channel");
    }

    public Long getStatusChannel() {
        return this.statusChannel;
    }

    public Long getGuildId() {
        return this.guildId;
    }

    public Long getReportChannel() {
        return reportChannel;
    }

    public long getWelcomeChannel() {
        return this.welcomeChannel;
    }

    @Override
    public String toString() {
        return "HyriConfig{" +
                "redis=" + redis +
                ", mongoDB=" + mongoDB +
                ", token='" + token + '\'' +
                ", roles=" + roles +
                ", categoryTickets=" + categoryTickets +
                ", ideaChannels=" + ideaChannels +
                ", voiceCustom=" + voiceCustom +
                ", statusChannel=" + statusChannel +
                ", welcomeChannel" + welcomeChannel +
                '}';
    }
}

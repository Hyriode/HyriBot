package fr.hyriode.hyribot.configuration;

import fr.hyriode.api.config.HyriMongoDBConfig;
import fr.hyriode.api.config.HyriRedisConfig;
import fr.hyriode.hyribot.command.HyriodeRole;
import fr.hyriode.hyribot.idea.IdeaType;
import fr.hyriode.hyribot.ticket.TicketReportType;

import java.util.HashMap;
import java.util.Map;

public class HyriConfig {

    private final HyriRedisConfig redis;
    private final HyriMongoDBConfig mongoDB;

    private final String token;
    private final Map<String, Long> roles;
    private final Map<String, Long> categoryTickets;
    private final Map<String, Long> ideaChannels;
    private final Map<String, Long> voiceCustom;

    public HyriConfig(HyriRedisConfig redisConfig, HyriMongoDBConfig mongoDBConfig) {
        this.redis = redisConfig;
        this.mongoDB = mongoDBConfig;
        this.token = System.getenv("TOKEN_HYRIBOT");
        this.roles = new HashMap<>();
        this.categoryTickets = new HashMap<>();
        this.ideaChannels = new HashMap<>();
        this.voiceCustom = new HashMap<>();
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

    public HyriRedisConfig getRedis() {
        return this.redis;
    }

    public HyriMongoDBConfig getMongoDB() {
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
}

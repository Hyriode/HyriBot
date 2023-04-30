package fr.hyriode.hyribot;

import fr.hyriode.api.HyriAPI;
import fr.hyriode.api.impl.application.HyriAPIImpl;
import fr.hyriode.api.impl.application.config.HyriAPIConfig;
import fr.hyriode.hyribot.command.HyriCommandManager;
import fr.hyriode.hyribot.configuration.HyriConfig;
import fr.hyriode.hyribot.giveaway.GiveawayManager;
import fr.hyriode.hyribot.interaction.button.ButtonManager;
import fr.hyriode.hyribot.interaction.modal.ModalManager;
import fr.hyriode.hyribot.interaction.selectmenu.SelectMenuManager;
import fr.hyriode.hyribot.listener.HyriListenerManager;
import fr.hyriode.hyribot.music.MusicManager;
import fr.hyriode.hyribot.ticket.TicketManager;
import fr.hyriode.hyribot.voicecustom.VoiceCustomManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.awt.*;
import java.util.Arrays;

public class HyriBot {

    public static final Color COLOR_HYRIODE = Color.decode("#83B7C4");
    public static final String NAME = "HyriBot";
    public static final String NAME_KEY = "hyribot";

    private final HyriConfig config;
    private final HyriAPI hyriAPI;
    private final JDA jda;

    private HyriListenerManager eventManager;
    private HyriCommandManager commandManager;
    private GiveawayManager giveawayManager;
    private TicketManager ticketManager;
    private SelectMenuManager selectMenuManager;
    private ButtonManager buttonManager;
    private ModalManager modalManager;
    private VoiceCustomManager voiceCustomManager;
    private MusicManager musicManager;

    public HyriBot(HyriConfig config) {
        System.out.println("Starting HyriBot...");

        this.config = config;
        this.hyriAPI = new HyriAPIImpl(new HyriAPIConfig.Builder()
                .withRedisConfig(this.config.getRedis())
                .withMongoDBConfig(this.config.getMongoDB())
                .withDevEnvironment(false)
                .withHyggdrasil(true)
                .build(), NAME);

        JDABuilder builder = JDABuilder.createDefault(this.config.getToken(), Arrays.asList(GatewayIntent.values()))
                .setStatus(OnlineStatus.ONLINE)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.NONE)
                .enableCache(Arrays.asList(CacheFlag.values()));

        this.initManagers();

        this.addListeners(builder);

        this.jda = builder.build();
    }

    private void initManagers() {
        this.giveawayManager = new GiveawayManager(this);
        this.ticketManager = new TicketManager(this);
        this.selectMenuManager = new SelectMenuManager(this);
        this.buttonManager = new ButtonManager(this);
        this.modalManager = new ModalManager(this);
        this.voiceCustomManager = new VoiceCustomManager(this);
        this.musicManager = new MusicManager(this);

        this.eventManager = new HyriListenerManager(this);
        this.commandManager = new HyriCommandManager(this);
    }

    private void addListeners(JDABuilder builder) {
        this.eventManager.getListeners().forEach(builder::addEventListeners);
        log(this, "Add " + this.eventManager.getListeners().size() + " listeners");
    }

    public JDA getJDA() {
        return jda;
    }

    public HyriConfig getConfig() {
        return config;
    }

    public HyriListenerManager getEventManager() {
        return eventManager;
    }

    public HyriCommandManager getCommandManager() {
        return commandManager;
    }

    public GiveawayManager getGiveawayManager() {
        return giveawayManager;
    }

    public TicketManager getTicketManager() {
        return ticketManager;
    }

    public SelectMenuManager getSelectMenuManager() {
        return selectMenuManager;
    }

    public ButtonManager getButtonManager() {
        return this.buttonManager;
    }

    public ModalManager getModalManager() {
        return this.modalManager;
    }

    public VoiceCustomManager getVoiceCustomManager() {
        return voiceCustomManager;
    }

    public MusicManager getMusicManager() {
        return this.musicManager;
    }

    public HyriAPI getHyriAPI() {
        return hyriAPI;
    }

    public void shutdown() {
        this.jda.shutdown();
    }

    public static void log(Object clazz, String message) {
        System.out.println("[" + clazz.getClass().getSimpleName() + "] " + message);
    }
}



package fr.hyriode.hyribot;

import com.google.gson.Gson;
import fr.hyriode.hyribot.configuration.HyriConfig;
import fr.hyriode.hyribot.utils.ReaderConsole;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Bootstrap {

    private static final Gson GSON = new Gson();
    private static final File FILE_CONFIG = new File("config_bot.json");
    private static final File FILE_DEV_CONFIG = new File("config_bot.dev.json");

    private static HyriBot hyriBot;
    private static HyriConfig lastConfig;
    private static boolean dev = false;

//    private static int size = 0;
//    private static int i = 0;
//
//    private static void ic() {
//        i++;
//        if(i >= size) {
//            System.out.println("All messages sent !");
//        }
//    }
//
//    private static void om() {
//        JDA jda = JDABuilder.createDefault("MTEwMTYzMDQ3NDUzNjg5NDU1NQ.GGbBfH.lQjeHQ0QHyZUVhVdzXIjHbEoT-sk7tRwXJsvUY", Arrays.asList(GatewayIntent.values()))
//                .setStatus(OnlineStatus.ONLINE)
//                .setMemberCachePolicy(MemberCachePolicy.ALL)
//                .setChunkingFilter(ChunkingFilter.NONE)
//                .enableCache(Arrays.asList(CacheFlag.values()))
//                .addEventListeners(new ListenerAdapter() {
//                    @Override
//                    public void onReady(@NotNull ReadyEvent event) {
//                        System.out.println("Ready");
//                        List<Member> members = event.getJDA().getGuildById(1070383145175756902L).getMembers().stream().filter(member -> !member.getUser().isBot()).toList();
//                        size = members.size();
//                        System.out.println("Members size : " + size);
//                        for (Member member : members) {
//
//                            System.out.println("Send message to " + member.getUser().getAsTag());
//                            try {
//                                member.getUser().openPrivateChannel().queue((c) -> {
//                                    System.out.println("Channel open " + member.getUser().getAsTag());
//                                    c.sendMessage("Attention, nous avons des problèmes avec notre discord LaMaisonRP principal, merci de rejoindre notre discord de secours : https://discord.gg/FWkMM8Va83").queue((s) -> {
//                                        System.out.println(i + ") Message send to " + member.getUser().getAsTag());
//                                        ic();
//                                    });
//                                });
//                            }catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }).build();
//        System.out.println(jda);
//    }

    public static void main(String[] args) throws IOException {
        //TODO change for remove the NPE
        System.out.println("Starting HyriBot...");
        if(args.length > 0 && args[0].equalsIgnoreCase("dev")) {
            dev = true;
        }

        ReaderConsole reader = new ReaderConsole();

        String config = "{}";
        if(dev) {
            if(FILE_DEV_CONFIG.exists()) {
                config = FileUtils.readFileToString(FILE_DEV_CONFIG, StandardCharsets.UTF_8);
            } else {
                throw new NullPointerException("File config not found.");
            }
        }else if(FILE_CONFIG.exists()) {
            config = FileUtils.readFileToString(FILE_CONFIG, StandardCharsets.UTF_8);
        }

        System.out.println(config);
        start(GSON.fromJson(config, HyriConfig.class));
        reader.start();
    }



    private static void start(HyriConfig config) {
        if(config == null) {
            throw new NullPointerException("The config is null.");
        }
        lastConfig = config;
        hyriBot = new HyriBot(lastConfig);
    }

    public static void restart(Boolean reloadConfig) throws IOException {
        if(reloadConfig == null)
            throw new NullPointerException("The reloadConfig is null.");
        if (hyriBot != null) {
            hyriBot.shutdown();
            hyriBot = null;
        }

        HyriConfig config = lastConfig;
        if (reloadConfig) {
            config = GSON.fromJson(FileUtils.readFileToString(FILE_CONFIG, StandardCharsets.UTF_8), HyriConfig.class);
            lastConfig = config;
        }

        start(config);
    }

    public static void stop() {
        if(hyriBot != null) {
            hyriBot.shutdown();
            hyriBot = null;
        }
    }

    public static HyriBot getCurrentBot() {
        return hyriBot;
    }

    public static boolean isDev() {
        return dev;
    }
}

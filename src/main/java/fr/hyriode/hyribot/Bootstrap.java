package fr.hyriode.hyribot;

import com.google.gson.Gson;
import fr.hyriode.hyribot.configuration.HyriConfig;
import fr.hyriode.hyribot.utils.ReaderConsole;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Bootstrap {

    private static final Gson GSON = new Gson();
    private static final File FILE_CONFIG = new File("config_bot.json");
    private static final File FILE_DEV_CONFIG = new File("config_bot.dev.json");

    private static HyriBot hyriBot;
    private static HyriConfig lastConfig;
    private static boolean dev = false;

    public static void main(String[] args) throws IOException {
        if(args[0].equalsIgnoreCase("dev"))
            dev = true;

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

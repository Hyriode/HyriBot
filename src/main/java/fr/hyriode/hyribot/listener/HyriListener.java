package fr.hyriode.hyribot.listener;

import fr.hyriode.hyribot.HyriBot;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class HyriListener extends ListenerAdapter {

    protected HyriBot bot;

    public HyriListener(HyriBot bot) {
        this.bot = bot;
    }

    public HyriBot getBot() {
        return bot;
    }
}

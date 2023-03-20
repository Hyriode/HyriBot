package fr.hyriode.hyribot.manager;

import fr.hyriode.hyribot.HyriBot;

public abstract class HyriManager {

    protected final HyriBot bot;

    public HyriManager(HyriBot bot) {
        this.bot = bot;
    }

}

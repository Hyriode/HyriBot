package fr.hyriode.hyribot.idea;

import fr.hyriode.hyribot.Bootstrap;

public enum IdeaType {
    DISCORD("Discord"),
    IN_GAME("en Jeu"),
    ;

    private final String name;

    IdeaType(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return "Idée " + this.name;
    }

    public String getName() {
        return this.name().toLowerCase();
    }

    public long getChannelId() {
        return Bootstrap.getCurrentBot().getConfig().getIdeaChannelId(this);
    }
}

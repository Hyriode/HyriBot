package fr.hyriode.hyribot.command.model.staff.report;

public enum PlaceType {
    DISCORD("Discord"),
    INGAME("En Jeu");

    private String name;

    PlaceType(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return this.name;
    }
}

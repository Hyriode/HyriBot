package fr.hyriode.hyribot.ticket;


public enum TicketType {
    ADMIN("Parler uniquement avec les administrateurs"),
    MOD("Parler avec l'équipe de modération"),
    ;

    private final String name;

    TicketType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}

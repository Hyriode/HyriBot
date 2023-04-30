package fr.hyriode.hyribot.ticket;


public enum TicketType {
    MOD("Parler avec l'équipe de modération"),
    ADMIN("Parler uniquement avec les administrateurs"),
    ;

    private final String name;

    TicketType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}

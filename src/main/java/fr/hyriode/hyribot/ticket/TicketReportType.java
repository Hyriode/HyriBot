package fr.hyriode.hyribot.ticket;

import fr.hyriode.hyribot.Bootstrap;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public enum TicketReportType {

    BUG("Rapport de bug", Emoji.fromUnicode("🐛")),
    SHOP("Problème boutique", Emoji.fromUnicode("🛒")),
    SANCTION("Sanction", Emoji.fromUnicode("⛔")),
    OTHER("Autre", Emoji.fromUnicode("❓"));
    ;

    private final String name;
    private final Emoji emoji;

    TicketReportType(String name, Emoji emoji) {
        this.name = name;
        this.emoji = emoji;
    }

    public String getName() {
        return this.name;
    }

    public Emoji getEmoji() {
        return this.emoji;
    }

    public SelectOption toSelectOption() {
        return SelectOption.of(this.name, this.name()).withEmoji(this.emoji);
    }

    public long getCategoryId() {
        return Bootstrap.getCurrentBot().getConfig().getCategoryTicketId(this);
    }

}

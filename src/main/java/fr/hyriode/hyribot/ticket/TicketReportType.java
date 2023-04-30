package fr.hyriode.hyribot.ticket;

import fr.hyriode.hyribot.Bootstrap;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public enum TicketReportType {

    BUG("Rapport de bug", Emoji.fromUnicode("🐛"), TicketType.MOD, true),
    SHOP("Problème boutique", Emoji.fromUnicode("🛒"), TicketType.MOD, true),
    SANCTION("Sanction", Emoji.fromUnicode("⛔"), TicketType.MOD, true),
    REPORT_STAFF("Report Staff", Emoji.fromUnicode("\uD83E\uDDF0"), TicketType.ADMIN),
    PARTNER("Postulation Partenaire", Emoji.fromUnicode("\uD83C\uDFA5"), TicketType.ADMIN),
    OTHER("Autre", Emoji.fromUnicode("❓"), TicketType.MOD, true),
    ;

    private final String name;
    private final Emoji emoji;
    private final TicketType type;
    private final boolean askPseudo;

    TicketReportType(String name, Emoji emoji, TicketType type) {
        this(name, emoji, type, false);
    }

    TicketReportType(String name, Emoji emoji, TicketType type, boolean askPseudo) {
        this.name = name;
        this.emoji = emoji;
        this.type = type;
        this.askPseudo = askPseudo;
    }

    public String getName() {
        return this.name;
    }

    public Emoji getEmoji() {
        return this.emoji;
    }

    public TicketType getType() {
        return type;
    }

    public boolean isAskPseudo() {
        return askPseudo;
    }

    public SelectOption toSelectOption() {
        return SelectOption.of(this.name, this.name()).withEmoji(this.emoji);
    }

    public long getCategoryId() {
        return Bootstrap.getCurrentBot().getConfig().getCategoryTicketId(this);
    }

}

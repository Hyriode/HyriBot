package fr.hyriode.hyribot.command;

import fr.hyriode.hyribot.Bootstrap;
import fr.hyriode.hyribot.configuration.HyriConfig;
import net.dv8tion.jda.api.audit.AuditLogEntry;

public enum HyriodeRole {
    STAFF(-2, "staff"),
    PLAYER(-1, "player"),
    HELPER(0, "helper"),
    DESIGNER(1, "designer"),
    BUILDER(2, "builder"),
    MODERATOR(3, "moderator"),
    DEVELOPER(4, "developer"),
    MANAGER(5, "manager"),
    ADMINISTRATOR(6, "administrator"),

    ;

    private final int id;
    private final String name;

    HyriodeRole(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getRoleId() {
        Long role = Bootstrap.getCurrentBot().getConfig().getRoleId(this);
        return role != null ? role : -1;
    }

    public boolean isSuperior(HyriodeRole role) {
        return role.getId() <= this.getId();
    }
}

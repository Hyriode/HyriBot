package fr.hyriode.hyribot.command.model.staff.report;

import net.dv8tion.jda.api.entities.Member;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Report {

    private Member reporter;
    private String victim;
    private String reason;
    private LocalDateTime date;
    private PlaceType placeType;

    public Report(Member reporter, Member victim, String reason, LocalDateTime date, PlaceType placeType) {
        this(reporter, victim.getUser().getAsTag(), reason, date, placeType);
    }

    public Report(Member reporter, String victim, String reason, LocalDateTime date, PlaceType placeType) {
        this.reporter = reporter;
        this.victim = victim;
        this.reason = reason;
        this.date = date;
        this.placeType = placeType;
    }

    public Member getReporter() {
        return reporter;
    }

    public String getVictim() {
        return victim;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getDateAsString() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm"));
    }

    public PlaceType getPlaceType() {
        return placeType;
    }

    public void setReporter(Member reporter) {
        this.reporter = reporter;
    }

    public void setVictim(String victim) {
        this.victim = victim;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public boolean setDate(String date) {
        try {
            this.date = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setPlaceType(PlaceType placeType) {
        this.placeType = placeType;
    }
}

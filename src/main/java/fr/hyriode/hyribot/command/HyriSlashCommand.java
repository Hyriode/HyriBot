package fr.hyriode.hyribot.command;

import fr.hyriode.api.rank.type.HyriStaffRankType;
import fr.hyriode.hyribot.HyriBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class HyriSlashCommand {

    protected final HyriBot bot;

    public HyriSlashCommand(HyriBot bot) {
        this.bot = bot;
    }

    public abstract void execute(SlashCommandInteractionEvent event);

    public abstract SlashCommandData getData();

    public String getName() {
        return this.getData().getName();
    }

    public String getDescription() {
        return this.getData().getDescription();
    }

    public HyriodeRole getPermission() {
        return HyriodeRole.PLAYER;
    }

}

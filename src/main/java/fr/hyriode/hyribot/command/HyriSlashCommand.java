package fr.hyriode.hyribot.command;

import fr.hyriode.hyribot.HyriBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.function.Consumer;

public abstract class HyriSlashCommand {

    protected final HyriBot bot;
    private final CommandData data;

    public HyriSlashCommand(HyriBot bot, CommandData data) {
        this.bot = bot;
        this.data = data;
    }

    public HyriSlashCommand(HyriBot bot, String name, String description) {
        this(bot, new CommandDataImpl(name, description));
    }

    public abstract void execute(SlashCommandInteractionEvent event);

    public CommandData getData() {
        return this.data;
    }

    public SlashCommandData getAsSlashCommandData() {
        return (SlashCommandData) this.data;
    }

    public String getName() {
        return this.getData().getName();
    }

    public String getDescription() {
        return this.getAsSlashCommandData().getDescription();
    }

    public HyriodeRole getPermission() {
        return HyriodeRole.PLAYER;
    }

    protected void addButton(String id, Consumer<ButtonInteractionEvent> action) {
        this.bot.getButtonManager().addButton(id, action);
    }
}

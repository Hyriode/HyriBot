package fr.hyriode.hyribot.listener.model;

import fr.hyriode.hyribot.Bootstrap;
import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriInteractionCommand;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.command.HyriodeRole;
import fr.hyriode.hyribot.listener.HyriListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends HyriListener {

    private int commandsInit = 0;

    public CommandListener(HyriBot bot) {
        super(bot);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        HyriSlashCommand command = this.bot.getCommandManager().getSlashCommand(event.getName());

        if(command != null && command.getPermission().isSuperior(HyriodeRole.PLAYER)) {
            command.execute(event);
        }
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        HyriSlashCommand command = this.bot.getCommandManager().getUserCommand(event.getName());

        if(command instanceof HyriInteractionCommand cmd
                && command.getPermission().isSuperior(HyriodeRole.PLAYER)) {
            cmd.execute(event);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        HyriBot.log(this, "Initialize commands" + (Bootstrap.isDev() ? " in dev mode" : "") + "...");

        int commandsSize = this.bot.getCommandManager().getCommands().size();
        final Guild guild = event.getJDA().getGuildById(this.bot.getConfig().getGuildId());

        if(guild != null) {
            this.bot.getCommandManager().getCommands().forEach(command -> {
                if(command instanceof HyriInteractionCommand) {
                    guild.upsertCommand(((HyriInteractionCommand) command).getButtonData()).queue(cmd -> {
                        this.commandInit(cmd.getName(), true, commandsSize);
                    });
                }
                guild.upsertCommand(command.getAsSlashCommandData()).queue(cmd -> {
                    this.commandInit(cmd.getName(), false, commandsSize);
                });
            });
        }
    }

    private void commandInit(String commandName, boolean isInteraction, int commandsSize) {
        if(!isInteraction) this.commandsInit++;

        HyriBot.log(this, "Command initialized: " + commandName + " (" + this.commandsInit + "/" + commandsSize + ")");

        if(this.commandsInit >= commandsSize) {
            HyriBot.log(this, this.commandsInit + "/" + commandsSize + " commands initialized!");
        }
    }
}

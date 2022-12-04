package fr.hyriode.hyribot.listener.model;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.command.HyriodeRole;
import fr.hyriode.hyribot.listener.HyriListener;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.jetbrains.annotations.NotNull;

public class CommandListener extends HyriListener {

    private int commandsInit = 0;

    public CommandListener(HyriBot bot) {
        super(bot);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        HyriSlashCommand command = this.bot.getCommandManager().getCommand(event.getName());

        //TODO permissions
        if(command != null && command.getPermission().isSuperior(HyriodeRole.PLAYER)) {
            command.execute(event);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        HyriBot.log(this, "Initialize commands...");

        this.bot.getCommandManager().getCommands().forEach(command -> {
            event.getJDA().upsertCommand(command.getData()).queue(cmd -> {
                this.commandInit(cmd.getName());
            });
        });
    }

    private void commandInit(String commandName) {
        this.commandsInit++;
        int commandsSize = this.bot.getCommandManager().getCommands().size();

        HyriBot.log(this, "Command initialized: " + commandName + " (" + this.commandsInit + "/" + commandsSize + ")");

        if(this.commandsInit >= commandsSize) {
            HyriBot.log(this, this.commandsInit + "/" + commandsSize + " commands initialized!");
        }
    }
}

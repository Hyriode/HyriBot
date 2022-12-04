package fr.hyriode.hyribot.command;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.utils.PackageScanner;

import java.util.ArrayList;
import java.util.List;

public class HyriCommandManager {

    private final List<HyriSlashCommand> commands = new ArrayList<>();

    public HyriCommandManager(HyriBot bot) {
        PackageScanner.scan(HyriSlashCommand.class, "fr.hyriode.hyribot.command.model").forEach(clazz -> {
            try {
                HyriSlashCommand command = (HyriSlashCommand) clazz.getConstructor(HyriBot.class).newInstance(bot);

                if(command.getData() == null) {
                    HyriBot.log(this, "Command '" + command.getClass().getSimpleName() + "' has no data!");
                    return;
                }

                this.registerCommand(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void registerCommand(HyriSlashCommand command) {
        this.commands.add(command);
        HyriBot.log(this, "Add command '" + command.getName() + "'");
    }

    public HyriSlashCommand getCommand(String name) {
        return this.commands.stream().filter(command -> command.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<HyriSlashCommand> getCommands() {
        return commands;
    }
}

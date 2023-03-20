package fr.hyriode.hyribot.command;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.manager.HyriManager;
import fr.hyriode.hyribot.utils.PackageScanner;

import java.util.ArrayList;
import java.util.List;

public class HyriCommandManager extends HyriManager {

    private final List<HyriSlashCommand> commands = new ArrayList<>();

    public HyriCommandManager(HyriBot bot) {
        super(bot);
        PackageScanner.scan(HyriSlashCommand.class, "fr.hyriode.hyribot.command.model", command -> {
            try {
                return command.getConstructor(HyriBot.class).newInstance(bot);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).forEach(this::registerCommand);
    }

    private void registerCommand(HyriSlashCommand command) {
        if(command == null) return;
        if(command.getData() == null) {
            HyriBot.log(this, "Command '" + command.getClass().getSimpleName() + "' has no data!");
            return;
        }

        this.commands.add(command);
        HyriBot.log(this, "Add command '" + command.getName() + "'");
    }

    public HyriSlashCommand getSlashCommand(String name) {
        return this.commands.stream().filter(command -> command.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public HyriSlashCommand getUserCommand(String name) {
        return this.commands.stream()
                .filter(command -> {
                    if(command instanceof HyriInteractionCommand cmd) {
                        return cmd.getButtonData().getName().equalsIgnoreCase(name);
                    }
                    return false;
                })
                .findFirst().orElse(null);
    }

    public List<HyriSlashCommand> getCommands() {
        return commands;
    }
}

package fr.hyriode.hyribot.command;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface HyriInteractionCommand {

    void execute(UserContextInteractionEvent event);

    CommandData getButtonData();

}

package fr.hyriode.hyribot.command.model;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class HelpCommand extends HyriSlashCommand {

    public HelpCommand(HyriBot bot) {
        super(bot, "help", "Affiche la liste des commandes.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        StringBuilder helpMsg = new StringBuilder();
        HyriEmbedBuilder em = new HyriEmbedBuilder();

        for (HyriSlashCommand command : this.bot.getCommandManager().getCommands()) {
            helpMsg.append("**/" + command.getName() + " •** " + command.getDescription() + "\n");
        }

        em.setTitle("Help");
        em.setDescription(helpMsg);

        event.replyEmbeds(em.build()).queue();
    }

}

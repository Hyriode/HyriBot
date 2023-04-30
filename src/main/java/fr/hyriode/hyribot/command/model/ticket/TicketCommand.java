package fr.hyriode.hyribot.command.model.ticket;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.interaction.selectmenu.SelectMenuManager;
import fr.hyriode.hyribot.ticket.MessageTicket;
import fr.hyriode.hyribot.ticket.TicketClosed;
import fr.hyriode.hyribot.ticket.TicketManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class TicketCommand extends HyriSlashCommand {
    public TicketCommand(HyriBot bot) {
        super(bot, new CommandDataImpl("ticket", "Manage tickets")
                .addSubcommandGroups(new SubcommandGroupData("list", "List a ticket")
                        .addSubcommands(new SubcommandData("progress", "Currents tickets"))
                        .addSubcommands(new SubcommandData("closed", "Closed tickets")
                                .addOption(OptionType.STRING, "user_id", "The user id", true))));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        System.out.println("demandé");
        if(event.getSubcommandGroup().equalsIgnoreCase("list")) {
            System.out.println("la liste");
            TicketManager ticketManager = this.bot.getTicketManager();
            switch (event.getSubcommandName()) {
                case "progress" -> {
                    if(ticketManager.getTicketsProgress().isEmpty()) {
                        event.reply("No tickets").queue();
                        return;
                    }
                    event.reply("List of tickets")
                            .addActionRow(this.bot.getSelectMenuManager().create((e) -> {
                                String id = e.getValues().get(0);
                                long requesterId = ticketManager.getTicketProgress(id).getRequesterId();
                                User user = this.bot.getJDA().getUserById(requesterId);
                                e.reply("Ticket " + user.getAsTag()).queue();
                            }, ticketManager.getTicketsProgress().stream().limit(25).map(ticketProgress -> SelectOption.of(ticketProgress.getId(), ticketProgress.getId())).toList()))
                            .queue();
                }
                case "closed" -> {
                    long memberTicket = event.getOption("user_id").getAsLong();
                    List<TicketClosed> ticketsClosed = ticketManager.getTicketsClosed().stream().filter(ticket -> ticket.getRequesterId() == memberTicket).toList();
                    if(ticketsClosed.isEmpty()) {
                        event.reply("No tickets").queue();
                        return;
                    }
                    event.reply("List of tickets")
                            .addActionRow(this.bot.getSelectMenuManager().create((e) -> {
                                String id = e.getValues().get(0);
                                List<String> messages = new ArrayList<>();
                                TicketClosed ticket = ticketManager.getTicketClosed(id);
                                List<MessageTicket> messagesTicket = ticket.getMessagesTicket().stream().limit(20).toList();
                                messages.add("Ticket " + id + ":");
                                for (MessageTicket messageTicket : messagesTicket) {
                                    String content = messageTicket.getContent();
                                    messages.add(messageTicket.getPseudo() + ": " + content);
                                }
                                try {
                                    File tempFile = File.createTempFile("prefixe", "suffixe");
                                    FileWriter writer = new FileWriter(tempFile);
                                    for (String chaine : messages) {
                                        writer.write(chaine + "\n");
                                    }
                                    writer.close();
                                    e.reply("Ticket `" + ticket.getId() + "`").addFiles(FileUpload.fromData(tempFile, "ticket.txt")).queue();

                                    tempFile.delete();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    e.reply("Erreur").queue();
                                }
                            }, ticketsClosed.stream().limit(25).map(ticketProgress -> SelectOption.of(ticketProgress.getId(), ticketProgress.getId())).toList()))
                            .queue();
                }
            }
        }
    }
}

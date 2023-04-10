package fr.hyriode.hyribot.listener.model.ticket;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriodeRole;
import fr.hyriode.hyribot.interaction.button.ButtonManager;
import fr.hyriode.hyribot.interaction.selectmenu.SelectMenuManager;
import fr.hyriode.hyribot.listener.HyriListener;
import fr.hyriode.hyribot.ticket.TicketManager;
import fr.hyriode.hyribot.ticket.TicketProgress;
import fr.hyriode.hyribot.ticket.TicketReportType;
import fr.hyriode.hyribot.ticket.TicketType;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import fr.hyriode.hyribot.utils.ThreadUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class TicketListener extends HyriListener {

    public TicketListener(HyriBot bot) {
        super(bot);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();

        if(msg.equals("TicKet") && event.getMember() != null && HyriodeRole.STAFF.hasRole(event.getMember())) {
            EmbedBuilder embedBuilder = new HyriEmbedBuilder();
            embedBuilder.setTitle("Créer un Ticket");
            embedBuilder.setDescription("Si vous voulez discuter avec le staff pour communiquer des bugs ou autre,\n" +
                    "Cliquez sur le bouton ci-dessous.");

            event.getChannel()
                    .sendMessageEmbeds(embedBuilder.build())
                    .setActionRow(Button.primary("ticket.create", "Créer un Ticket"))
                    .queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        String id = event.getButton().getId();
        if (guild == null || id == null) return;
        id = id.replace("ticket.", "");

        switch (id) {
            case "create": {
                event.reply("Quel type de ticket voulez vous traiter ?")
                        .setEphemeral(true).addActionRow(this.createSelectMenu()).queue();
            } break;
            case "close": {
                ButtonManager buttonManager = this.bot.getButtonManager();
                event.reply("Etes vous sûr de fermer ce ticket ?")
                        .setEphemeral(true)
                        .setActionRow(this.closeYes(buttonManager), this.closeNo(buttonManager)).queue();
            } break;
            case "add_member": {
                TicketManager ticketManager = this.bot.getTicketManager();
                TicketProgress ticket = ticketManager.getTicketProgress(event.getChannel().getIdLong());
                if (ticket == null) {
                    event.reply("Ce ticket n'existe pas").setEphemeral(true).queue();
                    return;
                }
                event.replyModal(this.bot.getModalManager().createMemberModal((e, member) -> {
                    if (ticket.getRequesterId() != member.getIdLong() && member.getRoles().stream()
                            .noneMatch(role -> role.getIdLong() == HyriodeRole.STAFF.getRoleId())) {
                        e.reply("Vous n'avez pas la permission de faire cette action sur ce ticket.")
                                .setEphemeral(true).queue();
                        return;
                    }

                    ticket.addMember(member);
                    e.reply(member.getAsMention() + " a été ajouté au ticket.").setEphemeral(true).queue();
                })).queue();
            } break;
            case "remove_member": {
                TicketManager ticketManager = this.bot.getTicketManager();
                TicketProgress ticket = ticketManager.getTicketProgress(event.getChannel().getIdLong());

                if (ticket == null) {
                    event.deferEdit()
                            .applyCreateData(new MessageCreateBuilder().setContent("Ce ticket n'existe pas").build())
                            .queue();
                    return;
                }
                long requesterId = ticket.getRequesterId();

                List<Member> members = ticket.getMembers(event.getGuild(), member -> member.getIdLong() != requesterId);
                if (members.isEmpty()) {
                    event.reply("Il n'y a aucun membre à retirer.").setEphemeral(true).queue();
                    return;
                }

                event.reply("Quel membre voulez vous retirer du ticket ?")
                        .setEphemeral(true)
                        .addActionRow(this.bot.getSelectMenuManager().create(e -> {
                            Member member = e.getMember();

                            if (requesterId != member.getIdLong() && member.getRoles().stream()
                                    .noneMatch(role -> role.getIdLong() == HyriodeRole.STAFF.getRoleId())) {
                                e.deferEdit().applyCreateData(new MessageCreateBuilder()
                                        .setContent("Vous n'avez pas la permission de faire cette action sur ce ticket")
                                        .build()).queue();
                                return;
                            }

                            Member memberToRemove = guild.getMemberById(e.getValues().get(0));
                            if (memberToRemove == null) {
                                e.deferEdit().applyCreateData(new MessageCreateBuilder()
                                        .setContent("Le membre n'existe pas")
                                        .build()).queue();
                                return;
                            }

                            ticket.removeMember(memberToRemove);
                            e.deferEdit().applyCreateData(new MessageCreateBuilder()
                                    .setContent(memberToRemove.getAsMention() + " a été retiré du ticket")
                                    .build()).queue();
                }, members.stream()
                                .map(member -> SelectOption.of(member.getUser().getAsTag(), member.getId())
                                        .withDescription("ID: " + member.getId()))
                                .toArray(SelectOption[]::new))).queue();
            } break;
        }
    }

    private SelectMenu createSelectMenu() {
        SelectMenuManager selectMenuManager = this.bot.getSelectMenuManager();

        return selectMenuManager.create(event -> {
            TicketReportType reportType = TicketReportType.valueOf(event.getValues().get(0));
            event.deferEdit().applyCreateData(new MessageCreateBuilder()
                    .setContent("Vous avez choisie " + reportType.getName() + ",\nA qui voulez vous s'adresser ?")
                    .addActionRow(selectMenuManager.create(selectEvent -> {
                        TicketType type = TicketType.valueOf(selectEvent.getValues().get(0));
                        TicketManager ticketManager = this.bot.getTicketManager();
                        TicketProgress ticket = ticketManager.createTicket(selectEvent.getMember(), reportType, type);

                        if(ticket == null) {
                            selectEvent.deferEdit().applyCreateData(new MessageCreateBuilder()
                                    .setContent("Vous ne pouvez pas créer plusieurs tickets à la fois.")
                                    .build()).queue();
                            return;
                        }

                        selectEvent.deferEdit().applyCreateData(new MessageCreateBuilder()
                                .setContent("Voici votre ticket: <#" + ticket.getChannelId() + ">")
                                .build()).queue();
                    }, Arrays.stream(TicketType.values())
                            .map(type -> SelectOption.of(type.getName(), type.name()))
                            .toArray(SelectOption[]::new)))
                    .build()).queue();
        }, Arrays.stream(TicketReportType.values())
                .map(TicketReportType::toSelectOption).toArray(SelectOption[]::new));
    }

    private ItemComponent closeYes(ButtonManager buttonManager) {
        return buttonManager.create("Oui", ButtonStyle.SUCCESS, btnEvent -> {
            TicketManager ticketManager = this.bot.getTicketManager();
            MessageChannel channel = btnEvent.getChannel();
            TicketProgress ticket = ticketManager.getTicketProgress(channel.getIdLong());
            Member member = btnEvent.getMember();

            if(ticket == null) {
                btnEvent.deferEdit().applyCreateData(new MessageCreateBuilder()
                        .setContent("Ce ticket n'existe pas.\n" +
                                "Aucun log ne sera enregistré.\n" +
                                "Le channel se supprimera dans 5 secondes.")
                        .build()).queue();
                ThreadUtil.taskLater(5000, () -> channel.delete().queue());
                return;
            }

            if(ticket.getRequesterId() != member.getIdLong() && member.getRoles().stream()
                    .noneMatch(role -> role.getIdLong() == HyriodeRole.STAFF.getRoleId())) {
                btnEvent.deferEdit().applyCreateData(new MessageCreateBuilder()
                        .setContent("Vous n'avez pas la permission de fermer ce ticket")
                        .build()).queue();
                return;
            }

            btnEvent.deferEdit().applyCreateData(new MessageCreateBuilder()
                    .setContent("Fermeture du ticket...")
                    .build()).queue();

            ticket.close(member, btnEvent.getMessageChannel().getIterableHistory().complete());
            channel.delete().queue();
        });
    }

    private Button closeNo(ButtonManager buttonManager) {
        return buttonManager.create("Non", ButtonStyle.DANGER, btnEvent -> {
            btnEvent.deferEdit().applyCreateData(new MessageCreateBuilder()
                    .setContent("Fermeture du ticket annulée")
                    .build()).queue();
        });
    }
}

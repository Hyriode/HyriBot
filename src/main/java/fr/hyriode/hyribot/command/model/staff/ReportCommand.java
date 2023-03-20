package fr.hyriode.hyribot.command.model.staff;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriInteractionCommand;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.command.model.staff.report.PlaceType;
import fr.hyriode.hyribot.command.model.staff.report.Report;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.Date;

public class ReportCommand extends HyriSlashCommand implements HyriInteractionCommand {

    public ReportCommand(HyriBot bot) {
        super(bot, new CommandDataImpl("report", "Report a player")
                .addOption(OptionType.STRING, "pseudo", "Le pseudo du joueur à reporter", true)
                .addOption(OptionType.STRING, "raison", "La raison du report", true)
                .addOption(OptionType.STRING, "date",
                        "La date du report si elle n'a pas été faite aujourd'hui, au format dd/MM/yyyy hh:mm"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping pseudoOption = event.getOption("pseudo");
        OptionMapping raisonOption = event.getOption("raison");
        String pseudo = pseudoOption != null ? pseudoOption.getAsString() : null;
        String reason = raisonOption != null ? raisonOption.getAsString() : null;
        OptionMapping dateOption = event.getOption("date");
        String date = dateOption != null
                ? dateOption.getAsString() : new SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date());
        OptionMapping placeOption = event.getOption("place") != null ? event.getOption("place") : null;
        PlaceType placeName = PlaceType.valueOf(placeOption != null ? placeOption.getAsString() : "DISCORD");
        this.sendReport(event.getGuild(), event.getUser(), pseudo, reason, date, placeName);
    }

    @Override
    public void execute(UserContextInteractionEvent event) {
        Member member = event.getMember();
        Member victim = event.getTargetMember();

        Modal modal = this.bot.getModalManager().createTextModal("Report de " + victim.getUser().getAsTag(), "Raison du report", (modalInteractionEvent, s) -> {
            Report report = new Report(member, victim, s, OffsetDateTime.now().toLocalDateTime(), PlaceType.DISCORD);

            modalInteractionEvent.reply(this.reportMessage(report)).setEphemeral(true).queue();
        });

        event.replyModal(modal).queue();

    }

    private MessageCreateData reportMessage(Report report) {
        return new MessageCreateBuilder()
                .setEmbeds(new HyriEmbedBuilder()
                        .setTitle("Report de " + report.getVictim())
                        .setDescription("Raison: " + report.getReason() +
                                "\nDate: " + report.getDateAsString() +
                                "\nPlatform: " + report.getPlaceType().getDisplayName())
                        .build())
                .setActionRow(this.bot.getButtonManager().create("Modifier Raison", ButtonStyle.PRIMARY, event -> {
                    Modal modal = this.bot.getModalManager().createTextModal("Modification de la raison", "Nouvelle raison", (modalInteractionEvent, s) -> {
                        report.setReason(s);
                        modalInteractionEvent.editMessage(MessageEditData.fromCreateData(this.reportMessage(report))).queue();
                    });
                    event.replyModal(modal).queue();
                }),this.bot.getButtonManager().create("Modifier Date", ButtonStyle.PRIMARY, event -> {
                    Modal modal = this.bot.getModalManager().createTextModal("Modification de la date", "Nouvelle date (Pattern: dd/MM/YYYY HH:mm)", (modalInteractionEvent, s) -> {
                        System.out.println("modal");
                        if(report.setDate(s)) {
                            System.out.println("date");
                            modalInteractionEvent.editMessage(MessageEditData.fromCreateData(this.reportMessage(report))).queue();
                        }
                    });
                    event.replyModal(modal).queue();
                }),this.bot.getButtonManager().create("Modifier Platform", ButtonStyle.PRIMARY, event -> {
                    SelectMenu selectMenu = this.bot.getSelectMenuManager().create(selectEvent -> {
                        PlaceType value = PlaceType.valueOf(selectEvent.getValues().get(0));

                        switch (value) {
                            case DISCORD -> report.setPlaceType(PlaceType.DISCORD);
                            case INGAME -> report.setPlaceType(PlaceType.INGAME);
                        }

                        selectEvent.editMessage(MessageEditData.fromCreateData(this.reportMessage(report))).queue();
                    }, Arrays.stream(PlaceType.values()).map(placeType -> SelectOption.of(placeType.getDisplayName(), placeType.name())).toList());
                    event.editMessage("Choisissez la plateforme").setActionRow(selectMenu).queue();
                }), this.bot.getButtonManager().create("Valider", ButtonStyle.SUCCESS, event -> {
                    Guild guild = event.getGuild();
                    if (guild == null) return;

                    TextChannel channel = guild.getTextChannelById(this.bot.getConfig().getReportChannel());

                    if(channel != null) {
                        channel.sendMessage(this.reportMessageDone(report)).queue();
                    }

                    event.editMessage(MessageEditData.fromCreateData(new MessageCreateBuilder().setContent("Le report a bien été envoyé").build())).queue();
                }))
                .build();
    }

    private MessageCreateData reportMessageDone(Report report) {
        return new MessageCreateBuilder()
                .setEmbeds(new HyriEmbedBuilder()
                        .setTitle("Report de " + report.getVictim())
                        .setAuthor("Reporteur " + report.getReporter().getUser().getAsTag(), null, report.getReporter().getEffectiveAvatarUrl())
                        .setDescription("Raison: " + report.getReason() + "\nDate: " + report.getDateAsString())
                        .build())
                .build();
    }

    @Override
    public CommandData getButtonData() {
        return new CommandDataImpl(Command.Type.USER, "Report");
    }

    private void sendReport(@Nullable Guild guild, User reporter, String pseudo, String reason, String date, PlaceType place) {
        if(guild == null || reporter == null || pseudo == null || reason == null || date == null || place == null) return;

        TextChannel reportChannel = guild.getTextChannelById(this.bot.getConfig().getReportChannel());
        EmbedBuilder embedBuilder = new HyriEmbedBuilder();
        embedBuilder.setAuthor("Report de " + reporter.getAsTag(), null, reporter.getAvatarUrl());
        embedBuilder.addField("Pseudo", pseudo, false);
        embedBuilder.addField("Raison", reason, false);
        embedBuilder.addField("Date", date, false);
        embedBuilder.addField("Lieu", place.getDisplayName(), false);

        if(reportChannel != null) {
            reportChannel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }
}

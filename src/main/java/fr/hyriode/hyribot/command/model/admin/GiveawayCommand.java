package fr.hyriode.hyribot.command.model.admin;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.giveaway.Giveaway;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import fr.hyriode.hyribot.utils.TimeUtil;
import fr.hyriode.hyribot.utils.giveaway.GiveawayUtil;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.List;

public class GiveawayCommand extends HyriSlashCommand {

    public GiveawayCommand(HyriBot bot) {
        super(bot, new CommandDataImpl("giveaway", "Gestion des giveaways")
                .addSubcommands(new SubcommandData("create", "Créer un giveaway")
                        .addOption(OptionType.CHANNEL, "channel", "Channel du giveaway.", true)
                        .addOption(OptionType.STRING, "price", "Prix du giveaway.", true)
                        .addOption(OptionType.STRING, "duration", "Durée du giveaway. Exemple: 15s, 5m, 8h, 3d", true)
                        .addOption(OptionType.INTEGER, "winners", "Nombre de gagnants.", true)
                )
                .addSubcommands(new SubcommandData("list", "Liste des giveaways"))
                .addSubcommands(new SubcommandData("finish", "Terminer un giveaway")
                        .addOption(OptionType.STRING, "id", "ID du giveaway.", true)
                ));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        try {
            String subcommandName = event.getSubcommandName();

            if(subcommandName == null) return;

            switch (subcommandName) {
                case "create": {
                    OptionMapping optionChannel = event.getOption("channel");
                    OptionMapping optionPrice = event.getOption("price");
                    OptionMapping optionDuration = event.getOption("duration");
                    OptionMapping optionWinners = event.getOption("winners");

                    GuildChannel channel = optionChannel.getAsChannel();

                    if(channel.getType() != ChannelType.TEXT) {
                        event.reply("Vous devez spécifier un salon textuel.").setEphemeral(true).queue();
                        return;
                    }

                    String price = optionPrice.getAsString();
                    long duration = TimeUtil.getTimeStringToMillis(optionDuration.getAsString());
                    long winners = optionWinners.getAsLong();
                    event.deferReply(true).setContent("Giveaway créé dans le channel " + channel.getAsMention()).queue();

                    this.bot.getGiveawayManager().createGiveaway(event.getMember().getIdLong(),
                            event.getGuild().getIdLong(),
                            channel.getIdLong(),
                            price, winners, duration,
                            System.currentTimeMillis());

                } break;
                case "list": {
                    List<Giveaway> giveaways = this.bot.getGiveawayManager().getGiveaways();
                    StringBuilder giveawayList = new StringBuilder();

                    if(giveaways.isEmpty()) {
                        giveawayList.append("Aucun giveaway existant.");
                    } else {
                        for (Giveaway giveaway : giveaways) {
                            giveawayList.append(GiveawayUtil.getStatus(giveaway) + " (" + giveaway.getId() + ") " + giveaway.getPrize() + "\n");
                        }
                    }

                    event.replyEmbeds(new HyriEmbedBuilder()
                            .setTitle("Liste des giveaways")
                            .setDescription(giveawayList)
                            .build()).queue();
                } break;
                case "finish": {
                    String giveawayId = event.getOption("id").getAsString();

                    this.bot.getGiveawayManager().stopGiveaway(giveawayId);
                } break;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

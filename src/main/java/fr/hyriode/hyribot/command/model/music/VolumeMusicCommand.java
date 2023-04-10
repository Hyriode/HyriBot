package fr.hyriode.hyribot.command.model.music;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

public class VolumeMusicCommand extends HyriSlashCommand {

    private static final String VOLUME_UP = "volume.up";
    private static final String VOLUME_DOWN = "volume.down";

    public VolumeMusicCommand(HyriBot bot) {
        super(bot, "volume", "Changer le volume de la musique en cours de lecture.");

        this.addButton(VOLUME_UP, (event) -> {
            if (this.bot.getMusicManager().isInSameChannel(event.getMember())) {
                int volume = this.bot.getMusicManager().getVolume(event.getGuild());
                if (volume < 100) {
                    this.bot.getMusicManager().volume(event.getGuild(), volume + 1);
                    event.editMessage(this.getVolumeMessage(event.getGuild()).build()).queue();
                }
            }
        });
        this.addButton(VOLUME_DOWN, (event) -> {
            if (this.bot.getMusicManager().isInSameChannel(event.getMember())) {
                int volume = this.bot.getMusicManager().getVolume(event.getGuild());
                if (volume >= 0) {
                    this.bot.getMusicManager().volume(event.getGuild(), volume - 1);
                    event.editMessage(this.getVolumeMessage(event.getGuild()).build()).queue();
                }
            }
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (this.bot.getMusicManager().isInSameChannel(event.getMember())) {
            event.reply(MessageCreateData.fromEditData(this.getVolumeMessage(event.getGuild()).build())).queue();
            return;
        }

        event.reply("Vous n'êtes pas dans le même channel que le bot.").queue();
    }

    private MessageEditBuilder getVolumeMessage(Guild guild) {
        int volume = this.bot.getMusicManager().getVolume(guild);

        return new MessageEditBuilder().setEmbeds(new HyriEmbedBuilder()
                        .setTitle("Volume")
                        .setDescription("Le volume est à " + volume + "%")
                        .build())
                .setActionRow(Button.success(VOLUME_UP, "➕"), Button.danger(VOLUME_DOWN, "➖"));
    }
}

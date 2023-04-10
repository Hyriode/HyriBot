package fr.hyriode.hyribot.command.model.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.music.GuildMusicManager;
import fr.hyriode.hyribot.music.MusicManager;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.util.List;

public class PlaylistMusicCommand extends HyriSlashCommand {

    private static final String RESUME_BUTTON = "music.resume";
    private static final String STOP_BUTTON = "music.stop";
    private static final String SKIP_BUTTON = "music.skip";

    public PlaylistMusicCommand(HyriBot bot) {
        super(bot, "playlist", "Gérer la playlists.");

        this.addButton(RESUME_BUTTON, event -> {
            Guild guild = event.getGuild();
            if(guild == null) return;

            this.bot.getMusicManager().togglePause(guild);
            event.editMessage(MessageEditBuilder.fromCreateData(this.getPlaylistMessage(guild).build()).build()).queue();
        });

        this.addButton(STOP_BUTTON, event -> {
            Member member = event.getMember();
            if(member == null) return;

            this.bot.getMusicManager().stop(member);
            event.editMessage(MessageEditBuilder.fromCreateData(this.getPlaylistMessage(member).build()).build()).queue();
        });

        this.addButton(SKIP_BUTTON, event -> {
            Member member = event.getMember();
            if(member == null) return;

            this.bot.getMusicManager().skipTrack(member);
            event.editMessage(MessageEditBuilder.fromCreateData(this.getPlaylistMessage(member).build()).build()).queue();
        });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        if(guild == null || member == null) return;

        if (this.bot.getMusicManager().isInSameChannel(event.getMember())) {
            event.reply(this.getPlaylistMessage(guild).build()).queue();
            return;
        }
        event.reply("Vous devez être dans le même channel que le bot pour utiliser cette commande.").queue();
    }

    private MessageCreateBuilder getPlaylistMessage(Member member) {
        Guild guild = member.getGuild();
        return this.getPlaylistMessage(guild);
    }

    private MessageCreateBuilder getPlaylistMessage(Guild guild) {
        EmbedBuilder embed = this.getListQueue(guild);
        return new MessageCreateBuilder().setEmbeds(embed.build())
                .setActionRow(
                        Button.secondary(RESUME_BUTTON, "⏯"),
                        Button.secondary(STOP_BUTTON, "⏹️"),
                        Button.secondary(SKIP_BUTTON, "⏭️")
                );
    }

    public EmbedBuilder getListQueue(Guild guild) {
        GuildMusicManager musicManager = this.bot.getMusicManager().getGuildAudioPlayer(guild);
        EmbedBuilder e = new HyriEmbedBuilder();
        e.setTitle("Musiques en attente (" + musicManager.getScheduler().getCurrentTrack() + ")");

        StringBuilder listTracks = new StringBuilder();
        AudioTrack audioTrack = musicManager.getPlayingTrack();
        if(audioTrack != null) {
            String now = MusicManager.getTime(audioTrack.getPosition());
            String max = MusicManager.getTime(audioTrack.getDuration());
            e.setDescription("> " + now + "/" + max + "** " + audioTrack.getInfo().title + "** " + "\n");
        }else {
            e.setDescription("Aucune musique n'est dans la liste");
            return e;
        }
        List<AudioTrack> queue = musicManager.getPlaylist();

        if (queue.size() != 0) {
            int i = 0;
            for (AudioTrack a : queue) {
                if(i < 9) {
                    listTracks.append(MusicManager.getTime(a.getDuration()) + " > **" + a.getInfo().title + "**\n");
                    i++;
                }
            }
            if(i > 9) {
                listTracks.append("Et " + (queue.size() - i) + " autres...");
            }
        }
        if(listTracks.toString().isEmpty())
            listTracks.append("Aucune autre musique est en attente");
        e.addField("", listTracks.toString(), false);
        return e;
    }

}

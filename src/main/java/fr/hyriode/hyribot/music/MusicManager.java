package fr.hyriode.hyribot.music;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.manager.HyriManager;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class MusicManager extends HyriManager {

    public AudioPlayerManager playerManager;
    public Map<Long, GuildMusicManager> musicManagers;
    public YouTube youTube;
    public SpotifyApi spotify;

    public MusicManager(HyriBot bot) {
        super(bot);
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);

        try {
            this.youTube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                   GsonFactory.getDefaultInstance(), null).setApplicationName("JDA Discord Bot").build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.spotify = new SpotifyApi.Builder()
                    .setClientId("921c8e7b814842b283f45273217ede11")
                    .setClientSecret("e77717d0b56e4162ab2d805c00dd3777")
                    .build();

            ClientCredentials clientCredentials = this.spotify.clientCredentials().build().execute();

            this.spotify.setAccessToken(clientCredentials.getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void searchLoadAndPlay(AudioChannel voiceChannel, Member member, InteractionHook ih, ServiceMusic serviceMusic, String search) {
        String trackUrl = this.search(search, serviceMusic);
        GuildMusicManager musicManager = getGuildAudioPlayer(voiceChannel.getGuild());
        EmbedBuilder e = new HyriEmbedBuilder();

        musicManager.getPlayer().setPaused(false);

        this.playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                e.setTitle("Musique ajoutée", trackUrl);
                e.setDescription("Title : **" + track.getInfo().title + "**\n" +
                        "Author : **" + track.getInfo().author + "**");
                e.setFooter("Time : " + getTime(track.getDuration()));
                ih.editOriginalEmbeds(e.build()).setContent("")
                        .setActionRow(this.getButtonReplay()).queue();
                play(musicManager, track);
            }

            private Button getButtonReplay() {
                return bot.getButtonManager().create("Rejouer la musique", ButtonStyle.PRIMARY, event -> {
                    play(member, ih, serviceMusic, search);
                    event.editButton(this.getButtonReplay()).queue();
                });
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                e.setTitle("Playlist ajouté");

                StringBuilder list = new StringBuilder();
                int i = 0;
                for (AudioTrack track : playlist.getTracks()) {
                    if (i < 6) {
                        String max = getTime(track.getDuration());
                        list.append("> " + max + " **" + track.getInfo().title + "** \n");
                        i++;
                    }
                    play(musicManager, track);
                }

                if (playlist.getTracks().size() - i > 0)
                    list.append("Et " + (playlist.getTracks().size() - i) + " autre(s)...");

                e.addField(playlist.getName(), list.toString(), true);

                ih.editOriginalEmbeds(e.build()).setContent("").queue();
            }

            @Override
            public void noMatches() {
                ih.editOriginal("Je n'ai pas trouvé : " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                exception.printStackTrace();
                ih.editOriginal("Je ne peux pas joué cette musique : " + exception.getMessage()).setContent("").queue();
            }
        });
    }

    public boolean stop(@Nullable Member member) {
        if(member != null) {
            Guild g = member.getGuild();

            if (isInSameChannel(member)) {
                GuildMusicManager musicManager = getGuildAudioPlayer(g);

                musicManager.getScheduler().getQueue().clear();
                musicManager.getScheduler().nextTrack();
                return true;
            }
        }
        return false;
    }

    private void play(GuildMusicManager musicManager, AudioTrack track) {
        musicManager.getScheduler().queue(track);
    }

    public void play(Member member, InteractionHook ih, ServiceMusic serviceMusic, String url) {
        if(member != null) {
            GuildVoiceState voiceState = member.getVoiceState();

            if(voiceState != null) {
                AudioChannel voiceChannel = voiceState.getChannel();

                if(voiceChannel != null) {
                    this.joinChannel(voiceChannel);
                    this.searchLoadAndPlay(voiceChannel, member, ih, serviceMusic, url);
                }
            }
        }
    }

    public void volume(Guild g, int volume) {
        GuildMusicManager musicManager = getGuildAudioPlayer(g);
        musicManager.setVolume(volume);
    }

    public void togglePause(Guild g){
        GuildMusicManager musicManager = getGuildAudioPlayer(g);
        musicManager.getPlayer().setPaused(!musicManager.getPlayer().isPaused());
    }

    public boolean isPaused(Guild g){
        GuildMusicManager musicManager = getGuildAudioPlayer(g);
        return musicManager.getPlayer().isPaused();
    }

    public boolean skipTrack(@Nullable Member member) {
        if(member != null) {
            Guild g = member.getGuild();

            if (this.isInSameChannel(member)) {
                GuildMusicManager musicManager = getGuildAudioPlayer(g);
                musicManager.getScheduler().nextTrack();
                return true;
            }
        }

        return false;
    }

    public int getVolume(Guild guild) {
        if(guild == null)
            return -1;
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        return musicManager.getVolume();
    }

    public boolean hasMemberInVoiceChannel(Member member) {
        return member.getVoiceState().getChannel() != null;
    }

    private String getTimeMusic(String vid, String dura) {
        int time = Integer.parseInt(vid.replace(",", ""));
        int timeDura = Integer.parseInt(dura.replace(",", ""));

        int position = time * timeDura / 100;
        //TODO ???

        return null;

    }

    public static String getTime(long timeMillis){
        int timeSecond = (int) (timeMillis / 1000);
        int hours = timeSecond / 3600;
        int minutes = (timeSecond % 3600) / 60;
        int seconds = timeSecond % 60;
        if(hours > 0)
            return hours + "h" + minutes + "m" + (seconds < 10 ? "0"+seconds : seconds) + "s";
        if(minutes > 0)
            return minutes + "m" + (seconds < 10 ? "0"+seconds : seconds) + "s";
        return seconds + "s";
    }

    public boolean isUrl(String input) {
        try {
            new URL(input);
            return true;
        } catch (MalformedURLException ignored) {
            return false;
        }
    }

    public String search(String input, ServiceMusic serviceMusic) {
        if(isUrl(input))
            return input;
        return switch (serviceMusic) {
            case YOUTUBE -> this.searchYoutube(input);
            case SPOTIFY -> this.searchSpotify(input);
        };
    }

    @Nullable
    public String searchYoutube(String input) {
        String youtubeKey = "AIzaSyDue85lHkKq0N9GLMBAyK5XOphlhqoDvrE";
        try {
            List<SearchResult> results = youTube.search().list(Arrays.asList("id", "snippet")).setQ(input).setMaxResults(1L)
                    .setType(Collections.singletonList("video"))
                    .setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)")
                    .setKey(youtubeKey).execute().getItems();
            if (!results.isEmpty()) {
                String videoId = results.get(0).getId().getVideoId();

                return "https://www.youtube.com/watch?v=" + videoId;
            }

        } catch (Exception ignored) {}
        return null;
    }

    public String searchSpotify(String input) {
        try {
            Paging<Track> result = this.spotify.searchTracks(input).build().execute();
            return result.getItems()[0].getIsPlayable() + " uiui";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isInSameChannel(Member member){
        if(member != null) {
            GuildVoiceState voiceState = member.getVoiceState();

            if (voiceState != null) {
                return isInChannel(member.getGuild().getSelfMember()) && voiceState.getChannel() != null &&
                        voiceState.getChannel().getIdLong() == voiceState.getChannel().getIdLong();
            }
        }
        return false;
    }

    public boolean isInChannel(Member member){
        GuildVoiceState voiceState = member.getVoiceState();

        if(voiceState != null) {
            return member.getVoiceState().getChannel() != null;
        }
        return false;
    }

    public boolean isInEmptyChannel(Guild guild){
        Member self = guild.getSelfMember();
        GuildVoiceState voiceState = self.getVoiceState();

        if(voiceState != null) {
            AudioChannel audioChannel = voiceState.getChannel();

            if(audioChannel != null) {
                return voiceState.getChannel().getMembers().stream().noneMatch(member -> member.getUser().isBot());
            }
        }
        return false;
    }

    public boolean joinChannel(AudioChannel voiceChannel) {
        Guild guild = voiceChannel.getGuild();
        Member self = guild.getSelfMember();

        if (!isInChannel(self) || isInEmptyChannel(guild)) {
            guild.getAudioManager().openAudioConnection(voiceChannel);
            return true;
        }
        return false;
    }

    public boolean joinChannel(Member member) {
        GuildVoiceState voiceState = member.getVoiceState();

        if(voiceState != null) {
            AudioChannel voiceChannel = voiceState.getChannel();

            if(voiceChannel != null) {
                return this.joinChannel(voiceChannel);
            }
        }
        return false;
    }

    public boolean leaveChannel(@Nullable Member member) {
        if(member != null) {
            Guild guild = member.getGuild();

            if (this.isInSameChannel(member)) {
                guild.getAudioManager().closeAudioConnection();
                return this.stop(member);
            }
        }
        return false;
    }

    public boolean pause(Member member) {
        Guild guild = member.getGuild();

        if(this.isInSameChannel(member)) {
            this.getGuildAudioPlayer(guild).getPlayer().setPaused(true);
            return true;
        }
        return false;
    }

    public boolean loop(Guild guild) {
        TrackScheduler scheduler = this.getGuildAudioPlayer(guild).getScheduler();
        scheduler.setLoop(!scheduler.isLoop());
        boolean isLoop = scheduler.isLoop();

        //TODO
//        scheduler.getQueue()

        return isLoop;
    }
}

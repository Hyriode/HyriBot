package fr.hyriode.hyribot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager {
    /**
     * Audio player for the guild.
     */
    private final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    private final TrackScheduler scheduler;

    /**
     * Creates a player and a track scheduler.
     *
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(AudioPlayerManager manager) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
    }

    public BlockingQueue<AudioTrack> getPlaylist(){
        return scheduler.getQueue();
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public AudioTrack getPlayingTrack() {
        return player.getPlayingTrack();
    }

    public int getVolume() {
        return player.getVolume();
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }
}
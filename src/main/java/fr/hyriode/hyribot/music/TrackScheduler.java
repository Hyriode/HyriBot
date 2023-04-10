package fr.hyriode.hyribot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final List<AudioTrack> queue;
    private boolean loop = false;
    private int currentTrack = 0;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new ArrayList<>();
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            this.queue.add(track);
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        if(this.loop) {
            if(this.queue.size() < this.currentTrack + 1)
                this.currentTrack += 1;
            else
                this.currentTrack = 0;
        } else {
            this.resetLoop();
        }
        this.player.startTrack(this.queue.get(this.currentTrack), false);
    }

    private void resetLoop() {
        if(this.currentTrack > 0) {
            if(this.currentTrack >= this.queue.size() - 1) {
                this.queue.clear();
            } else {
                for (int i = 0; i < this.currentTrack; i++) {
                    this.queue.remove(0);
                }
            }
        }
        this.currentTrack = 0;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
//        if(this.queue.isEmpty()){
//            System.out.println("queue is empty");
//            this.queue.addAll(this.queueLoop);
//            queue(this.queueLoop.get(0));
//        }
        if(!this.loop && endReason == AudioTrackEndReason.FINISHED) {
            this.queue.remove(track);
        }
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    public boolean setLoop(){
        this.loop = !loop;
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
        if(!loop) {
            this.resetLoop();
        }
    }

    public int getCurrentTrack() {
        return currentTrack;
    }

    public boolean isLoop() {
        return loop;
    }

    public List<AudioTrack> getQueue() {
        return this.queue;
    }
}

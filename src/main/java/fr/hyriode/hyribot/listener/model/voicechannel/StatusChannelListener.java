package fr.hyriode.hyribot.listener.model.voicechannel;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.configuration.HyriConfig;
import fr.hyriode.hyribot.listener.HyriListener;
import fr.hyriode.hyribot.utils.ThreadUtil;
import jdk.swing.interop.SwingInterOpUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import org.jetbrains.annotations.NotNull;

public class StatusChannelListener extends HyriListener {

    public StatusChannelListener(HyriBot bot) {
        super(bot);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        HyriConfig config = this.bot.getConfig();
        Long statusChannel = config.getStatusChannel();
        Guild guild = event.getJDA().getGuildById(config.getGuildId());

        if(statusChannel != null && guild != null) {
            try {
                Thread t = new Thread(() -> {
                    VoiceChannel voiceChannel = guild.getVoiceChannelById(statusChannel);

                    while (voiceChannel != null) {
                        try {
                            voiceChannel.getManager().setName(System.currentTimeMillis() + " tah").queue();
                            ThreadUtil.sleep(10*60000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        voiceChannel = guild.getVoiceChannelById(statusChannel);
                    }

                    System.out.println("The status channel was deleted or not found");
                });
                t.setDaemon(false);
                t.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

package fr.hyriode.hyribot.voicecustom;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriodeRole;
import fr.hyriode.hyribot.configuration.HyriConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class VoiceCustomManager {

    private final List<VoiceCustom> voiceCustoms = new ArrayList<>();

    private final HyriBot bot;

    public VoiceCustomManager(HyriBot bot) {
        this.bot = bot;
    }

    public void create(Member member) {
        HyriConfig config = this.bot.getConfig();
        Guild guild = member.getGuild();
        GuildVoiceState voiceState = member.getVoiceState();
        String id = UUID.randomUUID().toString();
        Category category = guild.getCategoryById(config.getVoiceCustomCategory());
        Role rolePlayer = guild.getRoleById(HyriodeRole.PLAYER.getRoleId());

        if(voiceState != null && voiceState.getChannel() != null
                && voiceState.getChannel().getIdLong() != config.getVoiceCustomChannel()
                || category == null || rolePlayer == null) {
            guild.kickVoiceMember(member).queue();
            return;
        }

        VoiceChannel channel = category
                .createVoiceChannel(member.getEffectiveName() + "'s Channel")
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT), null)
                .addPermissionOverride(rolePlayer, EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.VOICE_CONNECT))
                .complete();

        this.voiceCustoms.add(new VoiceCustom(id, member, channel));

        if(voiceState != null && voiceState.inAudioChannel()) {
            guild.moveVoiceMember(member, channel).queue();
        }
    }

    public List<VoiceCustom> getVoiceCustoms() {
        return voiceCustoms;
    }

    public VoiceCustom getVoiceCustomByIdChannel(long vc) {
        return this.voiceCustoms.stream().filter(vcm -> vcm.getChannelId() == vc).findFirst().orElse(null);
    }

    public void remove(AudioChannel voiceChannel) {
        VoiceCustom voiceCustom = this.getVoiceCustomByIdChannel(voiceChannel.getIdLong());
        this.voiceCustoms.remove(voiceCustom);
        voiceChannel.delete().queue();
    }

    public boolean contains(long idLong) {
        return this.voiceCustoms.stream().anyMatch(vcm -> vcm.getChannelId() == idLong);
    }
}

package fr.hyriode.hyribot.voicecustom;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriodeRole;
import fr.hyriode.hyribot.configuration.HyriConfig;
import fr.hyriode.hyribot.manager.HyriManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class VoiceCustomManager extends HyriManager {

    private final List<VoiceCustom> voiceCustoms = new ArrayList<>();

    public VoiceCustomManager(HyriBot bot) {
        super(bot);
    }

    public void create(Member member) {
        if(this.hasVoiceCustomByOwnerId(member.getIdLong())) {
            VoiceCustom voiceCustom = this.getVoiceCustomByOwnerId(member.getIdLong());
            VoiceChannel channel = member.getGuild().getVoiceChannelById(voiceCustom.getChannelId());

            if(channel != null) {
                member.getGuild().moveVoiceMember(member, channel).queue();
            }
            return;
        }

        HyriConfig config = this.bot.getConfig();
        Guild guild = member.getGuild();
        GuildVoiceState voiceState = member.getVoiceState();
        String id = UUID.randomUUID().toString();
        Category category = guild.getCategoryById(config.getVoiceCustomCategory());
        Role rolePlayer = guild.getRoleById(HyriodeRole.PLAYER.getRoleId());
        Role staffPlayer = guild.getRoleById(HyriodeRole.STAFF.getRoleId());

        if(voiceState != null && voiceState.getChannel() != null
                && voiceState.getChannel().getIdLong() != config.getVoiceCustomChannel()
                || category == null || rolePlayer == null || staffPlayer == null) {
            guild.kickVoiceMember(member).queue();
            return;
        }

        VoiceChannel channel = category
                .createVoiceChannel(member.getEffectiveName() + "'s Channel")
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT), null)
                .addPermissionOverride(staffPlayer, EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT), null)
                .addPermissionOverride(rolePlayer, EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.VOICE_CONNECT))
                .complete();

        this.voiceCustoms.add(new VoiceCustom(id, member, channel));

        if(voiceState != null && voiceState.inAudioChannel()) {
            guild.moveVoiceMember(member, channel).queue();
        }
    }

    private VoiceCustom getVoiceCustomByOwnerId(long idLong) {
        return this.voiceCustoms.stream()
                .filter(voiceCustom -> voiceCustom.getOwnerId() == idLong)
                .findFirst()
                .orElse(null);
    }

    private boolean hasVoiceCustomByOwnerId(long userId) {
        return this.voiceCustoms.stream().anyMatch(voiceCustom -> voiceCustom.getOwnerId() == userId);
    }

    public List<VoiceCustom> getVoiceCustoms() {
        return voiceCustoms;
    }

    public VoiceCustom getVoiceCustomByChannelId(long vc) {
        return this.voiceCustoms.stream().filter(vcm -> vcm.getChannelId() == vc).findFirst().orElse(null);
    }

    public void remove(AudioChannel voiceChannel) {
        VoiceCustom voiceCustom = this.getVoiceCustomByChannelId(voiceChannel.getIdLong());
        this.voiceCustoms.remove(voiceCustom);
        voiceChannel.delete().queue();
    }

    public boolean contains(long idLong) {
        return this.voiceCustoms.stream().anyMatch(vcm -> vcm.getChannelId() == idLong);
    }
}

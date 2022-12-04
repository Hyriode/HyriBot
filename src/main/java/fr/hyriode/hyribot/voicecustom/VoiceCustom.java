package fr.hyriode.hyribot.voicecustom;

import fr.hyriode.hyribot.Bootstrap;
import fr.hyriode.hyribot.command.HyriodeRole;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VoiceCustom {

    private transient Supplier<Guild> guildSupplier = () -> Bootstrap.getCurrentBot().getJDA().getGuildById(this.guildId);

    private String id;
    private long ownerId;
    private long guildId;
    private long channelId;
    private boolean isPublic;

    public VoiceCustom(String id, Member member, VoiceChannel channel) {
        this.id = id;
        this.ownerId = member.getIdLong();
        this.guildId = member.getGuild().getIdLong();
        this.channelId = channel.getIdLong();
    }

    public String getId() {
        return id;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public long getGuildId() {
        return guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setPublic(boolean isPublic, Runnable success) {
        Guild guild = guildSupplier.get();
        guild.getVoiceChannelById(this.channelId).getManager()
                .putRolePermissionOverride(HyriodeRole.PLAYER.getRoleId(),
                        isPublic ? EnumSet.of(Permission.VOICE_CONNECT) : null,
                        isPublic ? null : EnumSet.of(Permission.VOICE_CONNECT)).queue(__ -> success.run());
        this.isPublic = isPublic;
    }

    public List<Long> getWhitelist() {
        return this.guildSupplier.get().getVoiceChannelById(this.channelId).getMemberPermissionOverrides().stream()
                .map(perm -> perm.getMember().getIdLong()).collect(Collectors.toList());
    }

    public void addWhitelist(Member member) {
        guildSupplier.get().getVoiceChannelById(this.channelId).getManager()
                .putMemberPermissionOverride(member.getIdLong(), EnumSet.of(Permission.VOICE_CONNECT), null).queue();
    }

    public void removeWhitelist(Member member) {
        guildSupplier.get().getVoiceChannelById(this.channelId).getManager()
                .removePermissionOverride(member.getIdLong()).queue();
    }


    public void setOwnerId(long memberId) {
        this.ownerId = memberId;
    }

    public boolean isPublic() {
        return isPublic;
    }
}

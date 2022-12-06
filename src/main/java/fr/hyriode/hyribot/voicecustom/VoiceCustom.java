package fr.hyriode.hyribot.voicecustom;

import fr.hyriode.hyribot.Bootstrap;
import fr.hyriode.hyribot.command.HyriodeRole;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static fr.hyriode.hyribot.listener.model.voicechannel.VoiceCustomListener.*;

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

    public List<ItemComponent> getButtons() {
        return ActionRow.of(this.isPublic()
                        ? Button.danger(BUTTON_PANEL_PRIVATE, "Rendre privé")
                        : Button.success(BUTTON_PANEL_PUBLIC, "Rendre public"),
                Button.secondary(BUTTON_PANEL_NAME, "Gérer le nom"),
                Button.secondary(BUTTON_PANEL_LIMIT_USER, "Gérer la limite d'utilisateurs"),
                Button.secondary(BUTTON_PANEL_WHITELIST, "Gérer la whitelist"),
                Button.secondary(BUTTON_PANEL_MANAGE_MEMBERS, "Gérer les membres")
        ).getComponents();
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
                        isPublic ? EnumSet.of(Permission.VOICE_CONNECT, Permission.VIEW_CHANNEL) : null,
                        isPublic ? EnumSet.of(Permission.VIEW_CHANNEL) : EnumSet.of(Permission.VOICE_CONNECT))
                .queue(__ -> success.run());
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

    public boolean kick(VoiceChannel voiceChannel, Member memberFind) {
        if (memberFind != null && voiceChannel != null && voiceChannel.getMembers().contains(memberFind)) {
            memberFind.getGuild().kickVoiceMember(memberFind).queue();
            return true;
        }
        return false;
    }

    public boolean ban(VoiceChannel voiceChannel, Member memberFind) {
        if(memberFind != null && voiceChannel != null && voiceChannel.getMembers().contains(memberFind)) {
            voiceChannel.getManager().putPermissionOverride(memberFind, null, EnumSet.of(Permission.VOICE_CONNECT)).queue();
            return this.kick(voiceChannel, memberFind);
        }
        return false;
    }
}

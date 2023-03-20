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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

import static fr.hyriode.hyribot.listener.model.voicechannel.VoiceCustomListener.*;

public class VoiceCustom {

    private final transient Supplier<Guild> guildSupplier = () -> Bootstrap.getCurrentBot().getJDA().getGuildById(this.guildId);

    private final String id;
    private long ownerId;
    private long guildId;
    private final long channelId;
    private boolean isPublic;
    private final List<Long> whitelistedUsers = new ArrayList<>();
    private final List<Long> blacklistedUsers = new ArrayList<>();

    public VoiceCustom(String id, Member member, VoiceChannel channel) {
        this.id = id;
        this.ownerId = member.getIdLong();
        this.guildId = member.getGuild().getIdLong();
        this.channelId = channel.getIdLong();
    }

    public List<ItemComponent> getButtons() {
        return Arrays.asList(this.isPublic()
                        ? Button.danger(BUTTON_PANEL_PRIVATE, "Rendre privé")
                        : Button.success(BUTTON_PANEL_PUBLIC, "Rendre public"),
                Button.secondary(BUTTON_PANEL_NAME, "Gérer le nom"),
                Button.secondary(BUTTON_PANEL_LIMIT_USER, "Gérer la limite d'utilisateurs"),
                Button.secondary(BUTTON_PANEL_WHITELIST, "Gérer la whitelist"),
                Button.secondary(BUTTON_PANEL_MANAGE_MEMBERS, "Gérer les membres"));
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
        return this.whitelistedUsers;
    }

    public void addWhitelist(long member) {
        if(blacklistedUsers.contains(member) || whitelistedUsers.contains(member)) return;
        this.whitelistedUsers.add(member);
        this.guildSupplier.get().getVoiceChannelById(this.channelId).getManager()
                .putMemberPermissionOverride(member, EnumSet.of(Permission.VOICE_CONNECT), null).queue();
    }

    public void removeWhitelist(long member) {
        if(blacklistedUsers.contains(member) || !whitelistedUsers.contains(member)) return;
        this.whitelistedUsers.remove(member);
        this.guildSupplier.get().getVoiceChannelById(this.channelId).getManager()
                .removePermissionOverride(member).queue();
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
            this.blacklistedUsers.add(memberFind.getIdLong());
            this.whitelistedUsers.remove(memberFind.getIdLong());
            voiceChannel.getManager().putPermissionOverride(memberFind, null, EnumSet.of(Permission.VOICE_CONNECT)).queue();
            return this.kick(voiceChannel, memberFind);
        }
        return false;
    }
}

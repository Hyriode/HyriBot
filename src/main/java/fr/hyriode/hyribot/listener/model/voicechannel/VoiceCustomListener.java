package fr.hyriode.hyribot.listener.model.voicechannel;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriodeRole;
import fr.hyriode.hyribot.listener.HyriListener;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import fr.hyriode.hyribot.utils.StatusUtil;
import fr.hyriode.hyribot.voicecustom.VoiceCustom;
import fr.hyriode.hyribot.voicecustom.VoiceCustomManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.managers.channel.middleman.AudioChannelManager;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VoiceCustomListener extends HyriListener {

    private static final String PREFIX = "voice.custom.";

    private static final String BUTTON_PANEL = PREFIX + "panel";

    private static final String PREFIX_MANAGE = "voice.custom.manage.";

    public static final String BUTTON_PANEL_PRIVATE = PREFIX_MANAGE + "private";
    public static final String BUTTON_PANEL_PUBLIC = PREFIX_MANAGE + "public";
    public static final String BUTTON_PANEL_NAME = PREFIX_MANAGE + "name";
    public static final String BUTTON_PANEL_LIMIT_USER = PREFIX_MANAGE + "limit";
    public static final String BUTTON_PANEL_WHITELIST = PREFIX_MANAGE + "whitelist";
    public static final String BUTTON_PANEL_MANAGE_MEMBERS = PREFIX_MANAGE + "members";

    public static final String BUTTON_PANEL_WHITELIST_ADD_USER = BUTTON_PANEL_WHITELIST + ".add";
    public static final String BUTTON_PANEL_WHITELIST_REMOVE_USER = BUTTON_PANEL_WHITELIST + ".remove";
    public static final String BUTTON_PANEL_WHITELIST_REFRESH = BUTTON_PANEL_WHITELIST + ".refresh";

    public static final String BUTTON_PANEL_MANAGE_MEMBERS_REMOVE_USER = BUTTON_PANEL_MANAGE_MEMBERS + ".remove";
    public static final String BUTTON_PANEL_MANAGE_MEMBERS_BAN_USER = BUTTON_PANEL_MANAGE_MEMBERS + ".ban";

    private final Supplier<Long> channelId;
    private final Supplier<Long> categoryId;
    private final Supplier<VoiceCustomManager> voiceCustomManager;

    public VoiceCustomListener(HyriBot bot) {
        super(bot);
        this.channelId = () -> bot.getConfig().getVoiceCustomChannel();
        this.categoryId = () -> bot.getConfig().getVoiceCustomCategory();
        this.voiceCustomManager = bot::getVoiceCustomManager;
    }

    //For place the panel
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final Member member = event.getMember();

        if(event.getMessage().getContentRaw().equals("VoicEPaNel")
                && member != null && HyriodeRole.STAFF.hasRole(member)) {
            HyriEmbedBuilder embedBuilder = new HyriEmbedBuilder();
            embedBuilder.setTitle("Panel du Channel Vocal Temporaire");
            embedBuilder.setDescription("Cliquez sur le bouton pour afficher le panel de ton channel vocal temporaire !");
            event.getChannel().sendMessageEmbeds(embedBuilder.build())
                    .setActionRow(Button.success("voice.custom.panel", "Ouvrir le Panel"))
                    .queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        final Member member = event.getMember();
        if(member == null) return;

        final GuildVoiceState voiceState = member.getVoiceState();
        final VoiceCustomManager voiceCustomManager = this.bot.getVoiceCustomManager();

        final String id = event.getButton().getId();
        if(id == null || !id.startsWith(PREFIX)) return;

        if(voiceState == null || !voiceState.inAudioChannel() || voiceState.getChannel() == null
                || !voiceCustomManager.contains(voiceState.getChannel().getIdLong())) {
            event.reply("Vous n'êtes pas dans un channel vocal temporaire !").setEphemeral(true).queue();
            return;
        }

        final VoiceChannel voiceChannel = (VoiceChannel) voiceState.getChannel();
        final VoiceCustom voiceCustom = voiceCustomManager.getVoiceCustomByChannelId(voiceChannel.getIdLong());
        final Guild guild = voiceChannel.getGuild();

        if(id.equals(BUTTON_PANEL)) {
            try {
                ReplyCallbackAction reply = event.replyEmbeds(this.getEmbedPanel(voiceChannel, voiceCustom).build())
                        .setEphemeral(true);
                if(voiceCustom.getOwnerId() == member.getIdLong()){
                    reply.addActionRow(voiceCustom.getButtons());
                }
                reply.queue();
            }catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        if(voiceCustom.getOwnerId() != member.getIdLong()) {
            event.reply("Vous n'êtes pas le propriétaire de ce channel vocal !").setEphemeral(true).queue();
            return;
        }

        switch (id) {
            case BUTTON_PANEL_PRIVATE:
            case BUTTON_PANEL_PUBLIC: {
                boolean isPublic = !voiceCustom.isPublic();
                voiceCustom.setPublic(isPublic, () -> {
                    event.deferEdit().setEmbeds(this.getEmbedPanel(voiceChannel, voiceCustom).build())
                            .setActionRow(voiceCustom.getButtons())
                            .queue();
                });
            } break;
            case BUTTON_PANEL_NAME: {
                event.replyModal(this.bot.getModalManager().createTextModal(
                        "Modifier le nom du channel", "Nouveau nom du channel", (e, output) -> {
                    voiceChannel.getManager().setName(output).queue();
                    e.deferEdit().setEmbeds(this.getEmbedPanel(voiceChannel, voiceCustom).build()).queue();
                })).queue();
            } break;
            case BUTTON_PANEL_LIMIT_USER: {
                event.replyModal(this.bot.getModalManager().createNumberModal("Modifier la limite d'utilisateurs",
                        0, 99,
                        (e, output) -> {
                    voiceChannel.getManager().setUserLimit(output).queue(__ -> {
                        e.deferEdit().setEmbeds(this.getEmbedPanel(voiceChannel, voiceCustom).build()).queue();
                    });
                })).queue();
            } break;
            case BUTTON_PANEL_WHITELIST: {
                event.reply(this.getWhitelistPanel(voiceCustom, voiceChannel)).setEphemeral(true).queue();
            } break;
            case BUTTON_PANEL_WHITELIST_ADD_USER: {
                event.replyComponents(ActionRow.of(this.bot.getSelectMenuManager().createUser((selectEvent) -> {
                    selectEvent.editSelectMenu(selectEvent.getSelectMenu().withDisabled(true)).queue();
                    Member memberFind = (Member) selectEvent.getValues().get(0);
                    voiceCustom.addWhitelist(memberFind.getIdLong());
                    selectEvent.getInteraction().reply(memberFind.getAsMention() + " ajouté à la whitelist.").setEphemeral(true).queue();
                }))).setContent("Choisissez un membre à ajouté à la whitelist").setEphemeral(true).queue();
            } break;
            case BUTTON_PANEL_WHITELIST_REMOVE_USER: {
                if(voiceCustom.getWhitelist().size() == 0) {
                    event.reply("La whitelist est vide.").setEphemeral(true).queue();
                    return;
                }
                if(voiceCustom.getWhitelist().size() <= 25) {
                    event.replyComponents(ActionRow.of(this.bot.getSelectMenuManager().create(selectEvent -> {
                        selectEvent.editSelectMenu(selectEvent.getSelectMenu().withDisabled(true)).queue();
                        long memberId = Long.parseLong(selectEvent.getValues().get(0));
                        if(memberId != 0) {
                            voiceCustom.removeWhitelist(memberId);
                            selectEvent.getInteraction().reply("<@" + memberId + "> a bien été retiré.").setEphemeral(true).queue();
                            return;
                        }
                        selectEvent.getInteraction().reply("Membre non trouvé").setEphemeral(true).queue();
                    }, voiceCustom.getWhitelist().stream().map(aLong -> {
                        Member m = guild.getMemberById(aLong);
                        if(m == null) return SelectOption.of("Unknown#0000", "0");;
                        return SelectOption.of(m.getUser().getAsTag(), m.getId());
                    }).toArray(SelectOption[]::new)))).setContent("Choisissez un membre à ajouté à la whitelist")
                            .setEphemeral(true).queue();
                    return;
                }
                event.replyModal(this.bot.getModalManager().createMemberModal((modalEvent, memberFind) -> {
                    long memberId = memberFind.getIdLong();
                    voiceCustom.removeWhitelist(memberId);
                    modalEvent.editMessageEmbeds(this.getWhitelistPanel(voiceCustom, voiceChannel).getEmbeds()).queue();
                }, voiceCustom.getWhitelist().stream().map(guild::getMemberById).collect(Collectors.toList()))).queue();
            } break;
            case BUTTON_PANEL_WHITELIST_REFRESH: {
                event.deferEdit().setEmbeds(this.getWhitelistPanel(voiceCustom, voiceChannel).getEmbeds()).queue();
            } break;
            case BUTTON_PANEL_MANAGE_MEMBERS: {
                event.reply(this.getMembersPanel(voiceChannel)).setEphemeral(true).queue();
            } break;
            case BUTTON_PANEL_MANAGE_MEMBERS_REMOVE_USER: {
                event.replyComponents(ActionRow.of(this.bot.getSelectMenuManager().createUser(selectEvent -> {
                    selectEvent.editSelectMenu(selectEvent.getSelectMenu().withDisabled(true)).queue();
                    Member memberFind = (Member) selectEvent.getValues().get(0);
                    GuildVoiceState gvs = memberFind.getVoiceState();

                    if(member.getIdLong() == memberFind.getIdLong()) {
                        selectEvent.getInteraction().reply("Vous ne pouvez pas vous retirer vous même.").setEphemeral(true).queue();
                        return;
                    }

                    if(HyriodeRole.STAFF.hasRole(memberFind)) {
                        selectEvent.getInteraction().reply("Vous ne pouvez pas retirer un membre du staff.").setEphemeral(true).queue();
                        return;
                    }

                    if(gvs != null) {
                        AudioChannel vc = gvs.getChannel();
                        if(vc != null && vc.getIdLong() == voiceChannel.getIdLong()) {
                            if(voiceCustom.kick(voiceChannel, memberFind)) {
                                selectEvent.getInteraction().reply(memberFind.getAsMention() + " a été retiré du channel")
                                        .setEphemeral(true).queue();
                                return;
                            }
                            selectEvent.getInteraction().reply(memberFind.getAsMention() + " n'a pas pu être retiré du channel")
                                    .setEphemeral(true).queue();
                            return;
                        }
                        selectEvent.getInteraction().reply(memberFind.getAsMention() + " n'est pas dans le channel")
                                .setEphemeral(true).queue();
                    }
                }))).setEphemeral(true).queue();
            } break;
            case BUTTON_PANEL_MANAGE_MEMBERS_BAN_USER: {
                event.replyComponents(ActionRow.of(this.bot.getSelectMenuManager().createUser(selectEvent -> {
                    selectEvent.editSelectMenu(selectEvent.getSelectMenu().withDisabled(true)).queue();
                    Member memberFind = (Member) selectEvent.getValues().get(0);

                    if(member.getIdLong() == memberFind.getIdLong()) {
                        selectEvent.getInteraction().reply("Vous ne pouvez pas vous bannir vous même.").setEphemeral(true).queue();
                        return;
                    }

                    if(HyriodeRole.STAFF.hasRole(memberFind)) {
                        selectEvent.getInteraction().reply("Vous ne pouvez pas bannir un membre du staff.").setEphemeral(true).queue();
                        return;
                    }

                    if(voiceCustom.ban(voiceChannel, memberFind)) {
                        selectEvent.getInteraction().reply(memberFind.getAsMention() + " a été banni du channel")
                                .setEphemeral(true).queue();
                        return;
                    }
                    selectEvent.getInteraction().reply(memberFind.getAsMention() + " n'a pas pu être banni du channel")
                            .setEphemeral(true).queue();
                }))).setEphemeral(true).queue();
            } break;
        }
    }

    private MessageCreateData getMembersPanel(VoiceChannel voiceChannel) {
        int memberSize = voiceChannel.getMembers().size();
        EmbedBuilder embedBuilder = new HyriEmbedBuilder()
                .setTitle("Gestion des membres connecté - " + voiceChannel.getName())
                .setDescription(memberSize + " " + this.getEmojiByMemberSize(memberSize) + " membres connecté...");

        return new MessageCreateBuilder().setEmbeds(embedBuilder.build())
                .setActionRow(
                        Button.danger(BUTTON_PANEL_MANAGE_MEMBERS_REMOVE_USER, "Kick un membre du vocal")
                                .withDisabled(memberSize <= 1),
                        Button.danger(BUTTON_PANEL_MANAGE_MEMBERS_BAN_USER, "Ban un membre du vocal")
                                .withDisabled(memberSize <= 1))
                .build();
    }

    private String getEmojiByMemberSize(int size) {
        if(size <= 1) return "🔈";
        if(size <= 10) return "🔉";
        return "🔊";
    }

    private MessageCreateData getWhitelistPanel(VoiceCustom voiceCustom, VoiceChannel voiceChannel) {
        EmbedBuilder embedBuilder = new HyriEmbedBuilder();
        StringBuilder desc = new StringBuilder();
        embedBuilder.setTitle("Panel Whitelist - " + voiceChannel.getName());
        if(voiceCustom.getWhitelist().isEmpty()) {
            desc.append("Aucun utilisateur n'est whitelisté");
        } else {
            voiceCustom.getWhitelist().forEach(aLong -> desc.append("<@").append(aLong).append(">\n"));
        }
        embedBuilder.setDescription(desc);
        return new MessageCreateBuilder()
                .setEmbeds(embedBuilder.build())
                .setActionRow(
                        Button.success(BUTTON_PANEL_WHITELIST_ADD_USER, "Ajouter un utilisateur"),
                        Button.danger(BUTTON_PANEL_WHITELIST_REMOVE_USER, "Supprimer un utilisateur"),
                        Button.secondary(BUTTON_PANEL_WHITELIST_REFRESH, Emoji.fromUnicode("🔄"))
                )
                .build();
    }

    private MessageCreateData getMessagePanel(VoiceChannel voiceChannel, VoiceCustom voiceCustom) {
        return new MessageCreateBuilder().setEmbeds(this.getEmbedPanel(voiceChannel, voiceCustom).build()).build();
    }

    private EmbedBuilder getEmbedPanel(VoiceChannel voiceChannel, VoiceCustom voiceCustom) {
        EmbedBuilder embedBuilder = new HyriEmbedBuilder();
        StringBuilder desc = new StringBuilder();
        embedBuilder.setTitle("Panel - " + voiceChannel.getName());
        desc.append("Propriétaire : <@" + voiceCustom.getOwnerId() + ">" + "\n");
        desc.append("Statut : " + StatusUtil.getStatusVoiceChannel(voiceCustom.isPublic()) + "\n");
        desc.append("Limite utilisateurs : " + (voiceChannel.getUserLimit() == 0 ? "Infini" : voiceChannel.getUserLimit()) + "\n");
        desc.append("Nombre d'utilisateurs dans la whitelist : " + voiceCustom.getWhitelist().size());
        embedBuilder.setDescription(desc);
        return embedBuilder;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        final AudioChannel joinedChannel = event.getChannelJoined();
        final AudioChannel leftChannel = event.getChannelLeft();
        final Member member = event.getMember();

        //Left channel
        this.onLeftChannel(leftChannel, joinedChannel, member);
        //Joined channel
        this.onJoinChannel(joinedChannel, member);
    }

    private void onLeftChannel(AudioChannel leftChannel, AudioChannel joinChannel, Member member) {
        if(leftChannel != null && leftChannel.getIdLong() != this.getChannelId()) {
            final VoiceCustom voiceCustom = this.getVoiceCustomManager().getVoiceCustomByChannelId(leftChannel.getIdLong());
            final List<Member> members = leftChannel.getMembers().stream().filter(m -> !m.getUser().isBot())
                    .collect(Collectors.toList());

            if (voiceCustom != null) {
                if(members.size() == 0) {
                    this.getVoiceCustomManager().remove(leftChannel);
                    this.onJoinChannel(joinChannel, member);
                } else if(member.getIdLong() == voiceCustom.getOwnerId()) {
                    long oldOwnerId = voiceCustom.getOwnerId();
                    Member newOwner = members.get(ThreadLocalRandom.current().nextInt(members.size()));
                    AudioChannelManager<?, ?> channelManager = leftChannel.getManager();

                    voiceCustom.setOwnerId(newOwner.getIdLong());
                    channelManager.removePermissionOverride(oldOwnerId)
                            .putPermissionOverride(newOwner,
                                    EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT), null).queue();

                }
                return;
            }

            if(leftChannel.getParentCategoryIdLong() == this.getCategoryId()) {
                leftChannel.delete().queue();
            }
        }
    }

    private void onJoinChannel(AudioChannel joinedChannel, Member member) {
        if(joinedChannel != null) {
            if (joinedChannel.getIdLong() == this.getChannelId()) {
                this.getVoiceCustomManager().create(member);
            }
        }
    }

    public long getCategoryId() {
        return categoryId.get();
    }

    public long getChannelId() {
        return channelId.get();
    }

    public VoiceCustomManager getVoiceCustomManager() {
        return voiceCustomManager.get();
    }
}

package fr.hyriode.hyribot.listener.model.voicechannel;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.listener.HyriListener;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import fr.hyriode.hyribot.utils.StatusUtil;
import fr.hyriode.hyribot.voicecustom.VoiceCustom;
import fr.hyriode.hyribot.voicecustom.VoiceCustomManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class VoiceCustomListener extends HyriListener {

    private static final String PREFIX = "voice.custom.";

    private static final String BUTTON_PANEL = PREFIX + "panel";

    private static final String PREFIX_MANAGE = "voice.custom.manage.";

    private static final String BUTTON_PANEL_PRIVATE = PREFIX_MANAGE + "private";
    private static final String BUTTON_PANEL_PUBLIC = PREFIX_MANAGE + "public";
    private static final String BUTTON_PANEL_NAME = PREFIX_MANAGE + "name";
    private static final String BUTTON_PANEL_LIMIT_USER = PREFIX_MANAGE + "limit";
    private static final String BUTTON_PANEL_WHITELIST = PREFIX_MANAGE + "whitelist";

    private static final String BUTTON_PANEL_WHITELIST_ADD_USER = BUTTON_PANEL_WHITELIST + ".add";
    private static final String BUTTON_PANEL_WHITELIST_REMOVE_USER = BUTTON_PANEL_WHITELIST + ".remove";

    public VoiceCustomListener(HyriBot bot) {
        super(bot);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().equals("VoicEPaNel")) {
            HyriEmbedBuilder embedBuilder = new HyriEmbedBuilder();
            embedBuilder.setTitle("Panel du Channel Vocal Temporaire");
            embedBuilder.setDescription("Cliquez sur le bouton pour afficher le panel");
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

        if(voiceState == null || !voiceState.inAudioChannel() || voiceState.getChannel() == null
                || !voiceCustomManager.contains(voiceState.getChannel().getIdLong())) {
            event.reply("Vous n'êtes pas dans un channel vocal temporaire !").setEphemeral(true).queue();
            return;
        }
        final VoiceChannel voiceChannel = (VoiceChannel) voiceState.getChannel();
        final VoiceCustom voiceCustom = voiceCustomManager.getVoiceCustomByIdChannel(voiceChannel.getIdLong());

        final String id = event.getButton().getId();
        if(id == null || !id.startsWith(PREFIX)) return;

        switch (id) {
            case BUTTON_PANEL: {
                ReplyCallbackAction reply = event.replyEmbeds(this.getEmbedPanel(voiceChannel, voiceCustom).build()).setEphemeral(true);
                if(voiceCustom.getOwnerId() == member.getIdLong()){
                    reply.setActionRow(ActionRow.of(voiceCustom.isPublic() ? Button.danger(BUTTON_PANEL_PRIVATE, "Rendre privé") : Button.success(BUTTON_PANEL_PUBLIC, "Rendre public"),
                            Button.secondary(BUTTON_PANEL_NAME, "Gérer le nom"),
                            Button.secondary(BUTTON_PANEL_LIMIT_USER, "Gérer la limite d'utilisateurs"),
                            Button.secondary(BUTTON_PANEL_WHITELIST, "Gérer la whitelist")
                    ).getComponents());
                }
                reply.queue();
            } break;
            case BUTTON_PANEL_PRIVATE:
            case BUTTON_PANEL_PUBLIC: {//TODO
                boolean isPublic = !voiceCustom.isPublic();
                voiceCustom.setPublic(isPublic, () -> {
                    event.editButton(isPublic ? Button.success(BUTTON_PANEL_PUBLIC, "Rendre public") : Button.danger(BUTTON_PANEL_PRIVATE, "Rendre privé")).queue();
                    event.deferEdit().setEmbeds(this.getEmbedPanel(voiceChannel, voiceCustom).build())
                            .queue();
                });
            } break;
            case BUTTON_PANEL_NAME: {
                event.replyModal(this.bot.getModalManager().createTextModal("Modifier le nom du channel", "Nouveau nom du channel", (e, output) -> {
                    voiceChannel.getManager().setName(output).queue();
                    e.deferEdit().setEmbeds(this.getEmbedPanel(voiceChannel, voiceCustom).build()).queue();
                })).queue();
            } break;
            case BUTTON_PANEL_LIMIT_USER: {
                event.replyModal(this.bot.getModalManager().createNumberModal("Modifier la limite d'utilisateurs", 0, 99, (e, output) -> {
                    voiceChannel.getManager().setUserLimit(1).queue();
                    e.deferEdit().setEmbeds(this.getEmbedPanel(voiceChannel, voiceCustom).build()).queue();
                })).queue();
            } break;
            case BUTTON_PANEL_WHITELIST: {
                event.reply(this.getWhitelistPanel(voiceCustom, voiceChannel)).setEphemeral(true).queue();
            } break;
        }
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
                        Button.danger(BUTTON_PANEL_WHITELIST_REMOVE_USER, "Supprimer un utilisateur")
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
        desc.append("Limite utilisateurs : " + voiceChannel.getUserLimit() + "\n");
        desc.append("Nombre d'utilisateurs dans la whitelist : " + voiceCustom.getWhitelist().size());
        embedBuilder.setDescription(desc);
        return embedBuilder;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        AudioChannel joinedChannel = event.getChannelJoined();
        AudioChannel leftChannel = event.getChannelLeft();
        long channelId = this.bot.getConfig().getVoiceCustomChannel();
        VoiceCustomManager voiceCustomManager = this.bot.getVoiceCustomManager();

        //Joined channel
        if(joinedChannel != null) {
            if (joinedChannel.getIdLong() == channelId) {
                voiceCustomManager.create(event.getMember());
            }
        }
        //Left channel
        if(leftChannel != null) {
            VoiceCustom voiceCustom = voiceCustomManager.getVoiceCustomByIdChannel(leftChannel.getIdLong());
            List<Member> members = leftChannel.getMembers().stream().filter(member -> !member.getUser().isBot())
                    .collect(Collectors.toList());

            if (voiceCustom != null) {
                if(members.size() == 0) {
                    voiceCustomManager.remove(leftChannel);
                } else if(event.getMember().getIdLong() == voiceCustom.getOwnerId()) {
                    voiceCustom.setOwnerId(members.get(ThreadLocalRandom.current().nextInt(members.size())).getIdLong());
                }
            }
        }
    }
}
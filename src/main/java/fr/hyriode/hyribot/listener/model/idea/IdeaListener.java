package fr.hyriode.hyribot.listener.model.idea;

import fr.hyriode.hyribot.Bootstrap;
import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriodeRole;
import fr.hyriode.hyribot.idea.IdeaType;
import fr.hyriode.hyribot.listener.HyriListener;
import fr.hyriode.hyribot.utils.HyriEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class IdeaListener extends HyriListener {
    public IdeaListener(HyriBot bot) {
        super(bot);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().equals("TqtMeC")
                && event.getMember() != null && HyriodeRole.STAFF.hasRole(event.getMember())) {
            event.getChannel().sendMessageEmbeds(new HyriEmbedBuilder()
                            .setTitle("Donne ton idée !")
                            .setDescription("Tu as une idée pour le serveur ? Tu peux la proposer ici !\nChoisis le type d'idée que tu souhaites proposer.")
                            .build())
                    .setActionRow(
                            Button.primary("idea." + IdeaType.DISCORD, IdeaType.DISCORD.getDisplayName()),
                            Button.primary("idea." + IdeaType.IN_GAME, IdeaType.IN_GAME.getDisplayName())
                    ).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getButton().getId();
        if(id == null) return;

        if(id.startsWith("idea.")) {
            IdeaType ideaType = IdeaType.valueOf(id.split("\\.")[1].toUpperCase());

            event.replyModal(this.bot.getModalManager().createTextModal(ideaType.getDisplayName(), "Propose ton idée.", (e, text) -> {
                e.reply("Votre idée a bien été envoyée dans le channel <#" + ideaType.getChannelId() + ">")
                        .setEphemeral(true).queue();
                this.sendIdea(e.getMember(), text, ideaType);
            })).queue();
        }
    }

    private void sendIdea(Member member, String message, IdeaType type) {
        Guild guild = member.getGuild();
        TextChannel channel = guild.getTextChannelById(type.getChannelId());
        if(channel == null) return;

        channel.sendMessageEmbeds(this.getEmbed(member, message)).queue(msg -> {
            msg.addReaction(Emoji.fromUnicode("✅")).queue();
            msg.addReaction(Emoji.fromUnicode("❌")).queue();
            msg.createThreadChannel("Votre avis →").queue();
        });
    }

    private MessageEmbed getEmbed(Member member, String message) {
        return new EmbedBuilder()
                .setAuthor("Idée de " + member.getUser().getAsTag(), null, member.getUser().getAvatarUrl())
                .setColor(Color.yellow)
                .setDescription(message)
                .build();
    }
}

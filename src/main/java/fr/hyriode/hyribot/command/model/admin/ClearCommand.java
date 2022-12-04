package fr.hyriode.hyribot.command.model.admin;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.command.HyriSlashCommand;
import fr.hyriode.hyribot.utils.ThreadUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClearCommand extends HyriSlashCommand {
    public ClearCommand(HyriBot bot) {
        super(bot);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        OptionMapping optionUser = event.getOption("user");

        int number = event.getOption("number").getAsInt();
        Member member = optionUser != null ? optionUser.getAsMember() : null;
        MessageChannel channel = event.getMessageChannel();

        InteractionHook msg = event.reply("Waiting...").complete();

        int messagesToDelete = this.deleteMessages(number, member, channel, msg.retrieveOriginal().complete());

        msg.editOriginal(this.getMessage(member, messagesToDelete, guild))
                .queue(message -> ThreadUtil.taskLater(3000, () -> message.delete().queue()));
    }

    private String getMessage(Member member, int messagesToDelete, Guild guild) {
        if(member != null) {
            return messagesToDelete + " messages de " + member.getAsMention() + " ont été supprimés avec succès.";
        } else {
            return messagesToDelete + " messages ont été supprimé aveec succès";
        }
    }

    private int deleteMessages(int number, Member member, MessageChannel channel, Message... ignoredMessages) {
        if(number < 1) {
            number = 1;
        }
        int numberToDelete = Math.min(number, 100);
        List<Message> messagesToDelete = channel.getHistory().retrievePast(numberToDelete).complete()
                .stream().filter(message -> !Arrays.asList(ignoredMessages).contains(message)).collect(Collectors.toList());
        if(member != null) {
            messagesToDelete = messagesToDelete.stream()
                    .filter(message -> message.getMember().getIdLong() == member.getIdLong())
                    .collect(Collectors.toList());
        }

        channel.purgeMessages(messagesToDelete);

        if(messagesToDelete.size() > number) {
            return deleteMessages(number - 100, member, channel, ignoredMessages);
        }

        return messagesToDelete.size();
    }

    @Override
    public SlashCommandData getData() {
        return new CommandDataImpl("clear", "Clear le chat.");
    }
}

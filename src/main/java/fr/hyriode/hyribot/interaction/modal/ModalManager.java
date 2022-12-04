package fr.hyriode.hyribot.interaction.modal;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.interaction.HyriInteraction;
import fr.hyriode.hyribot.listener.HyriListener;
import fr.hyriode.hyribot.utils.MemberUtil;
import fr.hyriode.hyribot.utils.NumberUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModalManager {

    private final List<HyriInteraction<ModalInteractionEvent>> modals = new ArrayList<>();

    private final HyriBot bot;

    public ModalManager(HyriBot bot) {
        this.bot = bot;
    }

    public List<HyriInteraction<ModalInteractionEvent>> getModals() {
        return modals;
    }

    public HyriInteraction<ModalInteractionEvent> getModal(String id) {
        return this.modals.stream().filter(modal -> modal.getId().equals(id)).findFirst().orElse(null);
    }

    public Modal createMemberModal(BiConsumer<ModalInteractionEvent, Member> event) {
        String id = UUID.randomUUID().toString();
        Modal modal = Modal.create(id, "Trouver un membre").addActionRow(TextInput.create("member", "Pseudo du Membre ou son ID.", TextInputStyle.SHORT).build()).build();
        this.modals.add(new HyriInteraction<>(id, e -> {
            String input = e.getValue("member").getAsString();
            List<Member> members = MemberUtil.getMemberByNameOrId(e.getGuild(), input);

            if (members.isEmpty()) {
                e.reply("Aucun membre n'a cette ID ou ce pseudo.").setEphemeral(true).queue();
                return;
            }
            if (members.size() == 1) {
                event.accept(e, members.get(0));
                this.modals.remove(this.getModal(id));
                return;
            }
            if (members.size() <= 25) {
                e.reply("Veuillez choisir le membre attendu.").setEphemeral(true).setActionRow(this.getSelectMenuMembers(members, member -> {
                    event.accept(e, member);
                    this.modals.remove(this.getModal(id));
                })).queue();
                return;
            }
            e.reply("Trop de membre avec ce pseudo.").setEphemeral(true).setActionRow(this.getSelectMenuMembers(members, member -> {
                event.accept(e, member);
                this.modals.remove(this.getModal(id));
            })).queue();
        }));
        return modal;
    }

    private SelectMenu getSelectMenuMembers(List<Member> members, Consumer<Member> action) {
        return this.bot.getSelectMenuManager().create(event -> {
            String id = event.getValues().get(0);
            Member member = members.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);

            if(member != null) {
                event.deferEdit().applyCreateData(new MessageCreateBuilder().setContent("Membre selectionné: " + member.getAsMention()).build()).queue();
                action.accept(member);
                this.modals.remove(this.getModal(id));
            }
        }, members.stream().map(member -> SelectOption.of(member.getUser().getAsTag(), member.getId())
                .withDescription("ID: " + member.getId())).toArray(SelectOption[]::new));
    }

    public Modal createTextModal(String title, String titleInput, BiConsumer<ModalInteractionEvent, String> action) {
        String id = UUID.randomUUID().toString();
        Modal modal = Modal.create(id, title).addActionRow(TextInput.create("text", titleInput, TextInputStyle.SHORT).build()).build();
        this.modals.add(new HyriInteraction<>(id, e -> {
            action.accept(e, e.getValue("text").getAsString());
            this.modals.remove(this.getModal(id));
        }));
        return modal;
    }

    public Modal createNumberModal(String title, int minimum, int maximum, BiConsumer<ModalInteractionEvent, Integer> action) {
        String id = UUID.randomUUID().toString();
        Modal modal = Modal.create(id, title).addActionRow(TextInput.create("number", "Veuillez saisir un nombre entre " + minimum + " et " + maximum, TextInputStyle.SHORT).build()).build();
        this.modals.add(new HyriInteraction<>(id, e -> {
            String input = e.getValue("number").getAsString();
            if(NumberUtil.isNumber(input)) {
                int number = Integer.parseInt(input);
                if(number >= minimum && number <= maximum) {
                    action.accept(e, number);
                    this.modals.remove(this.getModal(id));
                }
            }
        }));
        return modal;
    }

    public static class Event extends HyriListener {
        public Event(HyriBot bot) {
            super(bot);
        }

        @Override
        public void onModalInteraction(ModalInteractionEvent event) {
            HyriInteraction<ModalInteractionEvent> modal = this.bot.getModalManager().getModal(event.getModalId());

            if(modal != null) {
                modal.event(event);
            }
        }
    }
}

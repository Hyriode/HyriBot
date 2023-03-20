package fr.hyriode.hyribot.interaction.selectmenu;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.interaction.HyriInteraction;
import fr.hyriode.hyribot.listener.HyriListener;
import fr.hyriode.hyribot.manager.HyriManager;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class SelectMenuManager extends HyriManager {

    private final List<HyriInteraction<StringSelectInteractionEvent>> stringSelectMenus = new ArrayList<>();
    private final List<HyriInteraction<EntitySelectInteractionEvent>> entitySelectMenus = new ArrayList<>();

    public SelectMenuManager(HyriBot bot) {
        super(bot);
    }

    public SelectMenu create(Consumer<StringSelectInteractionEvent> event, SelectOption... options) {
        String id = UUID.randomUUID().toString();
        SelectMenu selectMenu = StringSelectMenu.create(id).addOptions(options).build();
        this.stringSelectMenus.add(new HyriInteraction<>(id, event));
        return selectMenu;
    }

    public SelectMenu create(Consumer<StringSelectInteractionEvent> event, List<SelectOption> options) {
        return this.create(event, options.toArray(new SelectOption[0]));
    }

    public SelectMenu createUser(Consumer<EntitySelectInteractionEvent> event) {
        String id = UUID.randomUUID().toString();
        SelectMenu selectMenu = EntitySelectMenu.create(id, EntitySelectMenu.SelectTarget.USER).build();
        this.entitySelectMenus.add(new HyriInteraction<>(id, event));
        return selectMenu;
    }

    public List<HyriInteraction<StringSelectInteractionEvent>> getStringSelectMenus() {
        return stringSelectMenus;
    }

    public HyriInteraction<StringSelectInteractionEvent> getStringSelectMenu(String id) {
        return this.stringSelectMenus.stream().filter(selectMenu -> selectMenu.getId().equals(id)).findFirst().orElse(null);
    }

    public List<HyriInteraction<EntitySelectInteractionEvent>> getEntitySelectMenus() {
        return entitySelectMenus;
    }

    public HyriInteraction<EntitySelectInteractionEvent> getEntitySelectMenu(String id) {
        return this.entitySelectMenus.stream().filter(selectMenu -> selectMenu.getId().equals(id)).findFirst().orElse(null);
    }

    public static class Event extends HyriListener {

        public Event(HyriBot bot) {
            super(bot);
        }

        @Override
        public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
            SelectMenuManager manager = this.getBot().getSelectMenuManager();
            HyriInteraction<StringSelectInteractionEvent> selectMenu = manager.getStringSelectMenu(event.getSelectMenu().getId());

            if(selectMenu != null) {
                selectMenu.event(event);
                manager.getStringSelectMenus().remove(selectMenu);
            }
        }

        @Override
        public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
            SelectMenuManager manager = this.getBot().getSelectMenuManager();
            HyriInteraction<EntitySelectInteractionEvent> selectMenu = manager.getEntitySelectMenu(event.getSelectMenu().getId());

            if(selectMenu != null) {
                selectMenu.event(event);
                manager.getEntitySelectMenus().remove(selectMenu);
            }
        }
    }

}

package fr.hyriode.hyribot.interaction.selectmenu;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.interaction.HyriInteraction;
import fr.hyriode.hyribot.listener.HyriListener;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class SelectMenuManager {

    private final List<HyriInteraction<StringSelectInteractionEvent>> selectMenus = new ArrayList<>();

    private final HyriBot bot;

    public SelectMenuManager(HyriBot bot) {
        this.bot = bot;
    }

    public SelectMenu create(Consumer<StringSelectInteractionEvent> event, SelectOption... options) {
        String id = UUID.randomUUID().toString();
        SelectMenu selectMenu = StringSelectMenu.create(id).addOptions(options).build();
        this.selectMenus.add(new HyriInteraction<>(id, event));
        return selectMenu;
    }

    public List<HyriInteraction<StringSelectInteractionEvent>> getSelectMenus() {
        return selectMenus;
    }

    public HyriInteraction<StringSelectInteractionEvent> getSelectMenu(String id) {
        return this.selectMenus.stream().filter(selectMenu -> selectMenu.getId().equals(id)).findFirst().orElse(null);
    }

    public static class Event extends HyriListener {

        public Event(HyriBot bot) {
            super(bot);
        }

        @Override
        public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
            SelectMenuManager manager = this.getBot().getSelectMenuManager();
            HyriInteraction<StringSelectInteractionEvent> selectMenu = manager.getSelectMenu(event.getSelectMenu().getId());

            if(selectMenu != null) {
                selectMenu.event(event);
                manager.getSelectMenus().remove(selectMenu);
            }
        }
    }

}

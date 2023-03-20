package fr.hyriode.hyribot.interaction.button;

import fr.hyriode.hyribot.Bootstrap;
import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.interaction.HyriInteraction;
import fr.hyriode.hyribot.listener.HyriListener;
import fr.hyriode.hyribot.manager.HyriManager;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ButtonManager extends HyriManager {

    private final List<HyriInteraction<ButtonInteractionEvent>> buttons = new ArrayList<>();

    public ButtonManager(HyriBot bot) {
        super(bot);
    }

    public Button create(String label, ButtonStyle style, Consumer<ButtonInteractionEvent> event) {
        String id = UUID.randomUUID().toString();
        Button button = Button.of(style, id, label);
        this.buttons.add(new HyriInteraction<>(id, event));
        return button;
    }

    public HyriInteraction<ButtonInteractionEvent> getButton(String id) {
        return this.buttons.stream().filter(button -> button.getId().equals(id)).findFirst().orElse(null);
    }

    public List<HyriInteraction<ButtonInteractionEvent>> getButtons() {
        return buttons;
    }

    public void addButton(String id, Consumer<ButtonInteractionEvent> action) {
        this.buttons.add(new HyriInteraction<>(id, action));
    }

    public static class Event extends HyriListener {

        public Event(HyriBot bot) {
            super(bot);
        }

        @Override
        public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
            ButtonManager manager = this.bot.getButtonManager();
            HyriInteraction<ButtonInteractionEvent> button = manager.getButton(event.getButton().getId());

            if(button != null) {
                button.event(event);
            }
        }
    }


}

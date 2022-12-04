package fr.hyriode.hyribot.listener;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.interaction.button.ButtonManager;
import fr.hyriode.hyribot.interaction.modal.ModalManager;
import fr.hyriode.hyribot.interaction.selectmenu.SelectMenuManager;
import fr.hyriode.hyribot.utils.PackageScanner;
import net.dv8tion.jda.api.JDABuilder;

import java.util.ArrayList;
import java.util.List;

public class HyriListenerManager {

    private final List<HyriListener> listeners = new ArrayList<>();

    public HyriListenerManager(HyriBot bot) {
        this.registerListener(new SelectMenuManager.Event(bot));
        this.registerListener(new ButtonManager.Event(bot));
        this.registerListener(new ModalManager.Event(bot));
        PackageScanner.scan(HyriListener.class, "fr.hyriode.hyribot.listener.model").forEach(clazz -> {
            try {
                this.registerListener((HyriListener) clazz.getConstructor(HyriBot.class).newInstance(bot));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void registerListener(HyriListener listener) {
        this.listeners.add(listener);
        HyriBot.log(this, "Add listener '" + listener.getClass().getSimpleName() + "'");
    }

    public List<HyriListener> getListeners() {
        return listeners;
    }
}

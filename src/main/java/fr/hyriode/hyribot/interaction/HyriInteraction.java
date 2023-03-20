package fr.hyriode.hyribot.interaction;

import java.util.function.Consumer;

public class HyriInteraction<T> {

    private final String id;
    private final Consumer<T> event;

    public HyriInteraction(String id, Consumer<T> event) {
        this.id = id;
        this.event = event;
    }

    public String getId() {
        return id;
    }

    public void event(T event) {
        this.event.accept(event);
    }

}

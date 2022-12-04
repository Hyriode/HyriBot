package fr.hyriode.hyribot.listener.model;

import fr.hyriode.hyribot.HyriBot;
import fr.hyriode.hyribot.listener.HyriListener;
import net.dv8tion.jda.api.events.session.ReadyEvent;

public class ReadyListener extends HyriListener {

    public ReadyListener(HyriBot bot) {
        super(bot);
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Bot is ready!");
        System.out.print("> ");
    }

}

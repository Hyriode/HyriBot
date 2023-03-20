package fr.hyriode.hyribot.music;

import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public enum ServiceMusic {
    YOUTUBE,
    SPOTIFY

    ;

    public static Collection<? extends Command.Choice> getAsChoices() {
        return Arrays.stream(ServiceMusic.values())
                .map(serviceMusic -> new Command.Choice(serviceMusic.name(), serviceMusic.name()))
                .collect(Collectors.toList());
    }
}

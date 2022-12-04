package fr.hyriode.hyribot.utils;

import com.google.common.collect.Maps;
import fr.hyriode.hyribot.Bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ReaderConsole {

    private final Map<String, Consumer<String[]>> commands;

    public ReaderConsole() {
        this.commands = new HashMap<>();

        this.commands.put("help", s -> System.out.println("Available commands: " + this.commands.keySet()));
        this.commands.put("restart", s -> {
            System.out.println("Restarting...");
            try {
                Bootstrap.restart(Boolean.parseBoolean(s[0]));
            } catch (IOException e) {
                System.out.println("File config not found.");
            } catch (NullPointerException e) {
                System.out.println("Please, type 'true' or 'false'.");
            }
        });
        this.commands.put("stop", s -> {
            Bootstrap.stop();
            System.out.println("Bot stopped! (for stop the program, type 'exit')");
        });
        this.commands.put("exit", s -> {
            System.out.println("Bye!");
            Bootstrap.stop();
            System.exit(0);
        });
    }

    public void start() {
        Thread t = new Thread(() -> {
            while (true) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(System.in));

                System.out.print("> ");
                String name;
                try {
                    name = reader.readLine().split(" ")[0];
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("An exception occurred while reading the console.");
                    return;
                }

                boolean found = ((Supplier<Boolean>) () -> {
                    for (String cmd : commands.keySet()) {
                        if (name.equals(cmd)) {
                            commands.get(name).accept(name.replace(cmd, "").split(" "));
                            return true;
                        }
                    }
                    return false;
                }).get();

                if(!found) {
                    System.out.println("Unknown command.");
                }
            }
        });

        t.setDaemon(false);
        t.start();

    }
}

package fr.hyriode.hyribot.utils;

public class ThreadUtil {

    public static void taskLater(int delay, Runnable runnable) {
        new Thread(() -> {
            System.out.println("Tqt");
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {}
            System.out.println("Tah");
            runnable.run();
        }).start();
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

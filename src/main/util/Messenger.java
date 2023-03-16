package util;

import java.util.ArrayList;
import java.util.HashMap;

// something similar to C#'s Messenger
public class Messenger {
    private static final Messenger messenger = new Messenger();
    private final HashMap<String, ArrayList<MessengerListener>> listeners = new HashMap<>();

    public Messenger() {
    }

    public static Messenger getMain() {
        return messenger;
    }

    public void register(String name, MessengerListener listener) {
        if (!listeners.containsKey(name)) listeners.put(name, new ArrayList<>());
        listeners.get(name).add(listener);
    }

    public void send(String name) {
        send(name, null);
    }

    public void send(String name, Object obj) {
        if (listeners.containsKey(name)) {
            for (MessengerListener listener : listeners.get(name)) {
                listener.onMessage(null, obj);
            }
        }
    }

    public void send(String name, Object src, Object obj) {
        if (listeners.containsKey(name)) {
            for (MessengerListener listener : listeners.get(name)) {
                listener.onMessage(src, obj);
            }
        }
    }

    public void unregister(String name, MessengerListener listener) {
        if (listeners.containsKey(name)) listeners.get(name).remove(listener);
    }

    public void unregisterAll(String name) {
        if (listeners.containsKey(name)) listeners.get(name).clear();
    }

    public void unregisterAll(MessengerListener listener) {
        for (String name : listeners.keySet()) {
            listeners.get(name).remove(listener);
        }
    }

    public interface MessengerListener {
        void onMessage(Object src, Object obj);
    }

    public static class Constants {
        public static String CONNECTION_ESTABLISHED = "CONNECTION_ESTABLISHED";
    }
}

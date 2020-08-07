package de.tobiundmario.secrethitlermobilecompanion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameEvent;

public class GameLogChange {
    public static String EVENT_UPDATE = "event_update";
    public static String EVENT_DELETE = "event_delete";
    public static String NEW_EVENT = "new_event";

    private GameEvent event;
    private String type;

    private HashSet<String> servedTo;

    public GameLogChange(GameEvent event, String type) {
        servedTo = new HashSet<String>();

        this.event = event;
        this.type = type;
    }

    public JSONObject serve(String clientIP) throws JSONException {
        servedTo.add(clientIP);

        JSONObject obj = new JSONObject();
        obj.put("change_type", type);
        obj.put("event", event.getJSON());

        return obj;
    }

    public HashSet<String> getServedTo() {
        return servedTo;
    }

    public void addClientServedTo(String clientIP) {
        servedTo.add(clientIP);
    }
}

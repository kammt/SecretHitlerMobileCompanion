package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;

public class EventChange {
    public static String EVENT_UPDATE = "event_update";
    public static String EVENT_DELETE = "event_delete";
    public static String NEW_EVENT = "new_event";

    private GameEvent event;
    private String type;

    private HashSet<String> servedTo;

    public EventChange(GameEvent event, String type) {
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

    public GameEvent getEvent() {
        return event;
    }
}

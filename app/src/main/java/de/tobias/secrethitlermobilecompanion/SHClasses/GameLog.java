package de.tobias.secrethitlermobilecompanion.SHClasses;

import java.util.ArrayList;

public class GameLog {

    private ArrayList<GameEvent> log = new ArrayList<GameEvent>();

    public void addEvent(GameEvent event) {
        log.add(event);
    }

    public GameEvent[] getAllEvents() {
        return (GameEvent[]) log.toArray();
    }
}

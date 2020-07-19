package de.tobias.secrethitlermobilecompanion.SHClasses;

import java.util.HashMap;

public class PlayerList {
    private HashMap<String, Integer> playerList = new HashMap<String, Integer>();
    private int nextID = 0;

    public void addPlayer(String name) {
        playerList.put(name, nextID++);
    }

    public Integer getID(String name) {
        return playerList.get(name).intValue();
    }

    public HashMap<String, Integer> getPlayerList() {
        return playerList;
    }
}

package de.tobias.secrethitlermobilecompanion.SHClasses;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerList {
    private static ArrayList<String> playerList = new ArrayList<String>();
    private static int nextID = 0;

    public static void addPlayer(String name) {
        playerList.add(name);
    }

    public static ArrayList<String> getPlayerList() {
        return playerList;
    }
}

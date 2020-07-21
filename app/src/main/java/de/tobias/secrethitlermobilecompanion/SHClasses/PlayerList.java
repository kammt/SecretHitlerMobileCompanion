package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import de.tobias.secrethitlermobilecompanion.CardRecyclerViewAdapter;
import de.tobias.secrethitlermobilecompanion.PlayerRecyclerViewAdapter;

public class PlayerList {
    private static ArrayList<String> playerList = new ArrayList<String>();
    private static int nextID = 0;

    private static PlayerRecyclerViewAdapter playerRecyclerViewAdapter;
    private static RecyclerView playerListRecyclerView;

    public static void addPlayer(String name) {
        playerList.add(name);
    }

    public static ArrayList<String> getPlayerList() {
        return playerList;
    }

    public static void setupPlayerList(RecyclerView playerListRecyclerView, Context context) {
        playerRecyclerViewAdapter = new PlayerRecyclerViewAdapter(playerList, context);
        playerListRecyclerView.setAdapter(playerRecyclerViewAdapter);
    }
}

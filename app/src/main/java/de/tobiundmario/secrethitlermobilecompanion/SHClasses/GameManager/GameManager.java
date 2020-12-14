package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import java.util.ArrayList;
import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrack;

import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager.eventList;

public class GameManager {

    public static int liberalPolicies = 0;
    public static int fascistPolicies = 0;
    public static int electionTracker = 0;
    public static FascistTrack gameTrack = null;

    private static boolean gameStarted;

    public static boolean isGameStarted() {
        return gameStarted;
    }

    public static void setGameStarted(boolean isGameStarted) {
        gameStarted = isGameStarted;
        RecyclerViewManager.swipeEnabled = isGameStarted;
        GameEventsManager.editingEnabled = isGameStarted;
        if(isGameStarted) RecyclerViewManager.setupSwipeToDelete();
    }

    /**
     * Receives a list of players that are deselected and updates the indexes of blurred events accordingly
     * @param hiddenPlayers
     */
    public static void blurEventsInvolvingHiddenPlayers(List<String> hiddenPlayers) {
        ArrayList<Integer> cardIndexesToBlur = new ArrayList<>();

        for(int i = 0; i < eventList.size(); i++) {

            if(!eventList.get(i).isSetup && eventList.get(i).allInvolvedPlayersAreUnselected(hiddenPlayers)) {
                cardIndexesToBlur.add(i);
            }
        }
        GameEventsManager.hiddenEventIndexes = cardIndexesToBlur; //Update the static ArrayList, making it accessible to the RecyclerViewAdapter. When rendering a view (which was null before), it will look up if it has to be blurred or not
        RecyclerViewManager.getCardListAdapter().notifyDataSetChanged();
    }




}


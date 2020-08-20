package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.MainActivity;
import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.CardRecyclerViewAdapter;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.PolicyPeekEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.SpecialElectionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.VoteEvent;

public class GameLog {

    public static int legSessionNo = 1;
    static private RecyclerView cardList;
    static List<GameEvent> eventList;
    private static boolean gameStarted;

    private static CardRecyclerViewAdapter cardListAdapter;

    public static ArrayList<Integer> hiddenEventIndexes;

    private static JSONArray arr;
    private static Context c;

    public static int liberalPolicies = 0;
    public static int fascistPolicies = 0;
    public static FascistTrack gameTrack = null;

    public static boolean swipeEnabled = false;

    public static boolean executionSounds, policySounds, endSounds;

    public static boolean isGameStarted() {
        return gameStarted;
    }

    public static int getLiberalPolicies() {
        return liberalPolicies;
    }

    public static CardRecyclerViewAdapter getCardListAdapter() {
        return cardListAdapter;
    }

    public static void destroy() {
        c = null;
        cardList = null;
        cardListAdapter = null;
        eventList = null;
        hiddenEventIndexes = null;
        arr = null;
        gameTrack = null;
    }

    public static void notifySetupPhaseLeft(GameEvent event) {
        int position;

        //We have to differentiate between two separate scenarios. If the event left the Editing phase, we want to change the JSON data at a specific position. If it left setup phase, we just want to add it to the array
        if(event.isEditing) {

            position = eventList.indexOf(event);
            event.isEditing = false;
            try {
                arr.put(eventList.indexOf(event), event.getJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONManager.addGameLogChange(new GameLogChange(event, GameLogChange.EVENT_UPDATE));

        } else {

            position = eventList.size();
            try {
                arr.put(event.getJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONManager.addGameLogChange(new GameLogChange(event, GameLogChange.NEW_EVENT));
        }
        //Nevertheless, we need to update the RecyclerViewItem
        cardListAdapter.notifyItemChanged(position);
        if(event.getClass() == LegislativeSession.class) processPolicyChange((LegislativeSession) event, false);

        //Something changed - it's backup time!
        try {
            backupToCache();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void remove(GameEvent event) {
        int position = eventList.indexOf(event);
        if(!event.isSetup) arr.remove(eventList.indexOf(event));
        eventList.remove(position);
        cardListAdapter.notifyItemRemoved(position);

        if(event.getClass() == ExecutionEvent.class && !event.isSetup) ((ExecutionEvent) event).resetOnRemoval();
        if(event.getClass() == LoyaltyInvestigationEvent.class && !event.isSetup) ((LoyaltyInvestigationEvent) event).resetOnRemoval();
        if(event.getClass() == LegislativeSession.class && !event.isSetup) {
            reSetSessionNumber();
            processPolicyChange((LegislativeSession) event, true);
        }
    }

    public static void undoRemoval(GameEvent event, int oldPosition) {
        try {
            arr.put(oldPosition, event.getJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventList.add(oldPosition, event);
        cardListAdapter.notifyItemInserted(oldPosition);

        if(event.getClass() == ExecutionEvent.class && !event.isSetup) ((ExecutionEvent) event).undoRemoval();
        if(event.getClass() == LoyaltyInvestigationEvent.class && !event.isSetup) ((LoyaltyInvestigationEvent) event).undoRemoval();
        if(event.getClass() == LegislativeSession.class && !event.isSetup) {
            reSetSessionNumber();
            processPolicyChange((LegislativeSession) event, false);
        }
    }

    public static void setGameStarted(boolean isGameStarted) {
        gameStarted = isGameStarted;
        swipeEnabled = isGameStarted;
        if(isGameStarted) setupSwipeToDelete();
    }

    public static void initialise(RecyclerView recyclerView, Context context) {
        eventList = new ArrayList<>();
        hiddenEventIndexes = new ArrayList<>();
        arr = new JSONArray();
        legSessionNo = 1;

        c = context;

        cardList = recyclerView;
        cardListAdapter = new CardRecyclerViewAdapter(eventList);
        cardList.setAdapter(cardListAdapter);

        //Reset the policy-count
        liberalPolicies = 0;
        fascistPolicies = 0;

        gameTrack = null;
    }

    public static void addEvent(GameEvent event) {
        eventList.add(event);
        cardListAdapter.notifyItemInserted(eventList.size() - 1);
        try {
            if(!event.isSetup) arr.put(event.getJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(event.getClass() == LegislativeSession.class && !event.isSetup) processPolicyChange((LegislativeSession) event, false);
        if(event.isSetup) cardList.smoothScrollToPosition(eventList.size() - 1);
    }

    public static void blurEventsInvolvingHiddenPlayers(ArrayList<String> hiddenPlayers) {
        ArrayList<Integer> cardIndexesToBlur = new ArrayList<>();

        for(int i = 0; i < eventList.size(); i++) {
            View card = cardList.getLayoutManager().findViewByPosition(i);
            if(!eventList.get(i).isSetup && eventList.get(i).allInvolvedPlayersAreUnselected(hiddenPlayers)) {
                cardIndexesToBlur.add(i);
                if(card != null) card.setAlpha((float) 0.5);    //When the Card is not in view, the view returned will be null. Hence, we have to check
            } else if (card != null && card.getAlpha() < 1) {   //If it shouldn't be blurred, we have to check if it is in view (not null) and is still blurred. If so, we have to un-blur it
                card.setAlpha(1);
            }
        }
        hiddenEventIndexes = cardIndexesToBlur; //Update the static ArrayList, making it accessible to the RecyclerViewAdapter. When rendering a view (which was null before), it will look up if it has to be blurred or not
    }

    public static void processPolicyChange(LegislativeSession legislativeSession, boolean removed) {
        if(legislativeSession.getVoteEvent().getVotingResult() == VoteEvent.VOTE_FAILED) return;
        if(legislativeSession.getClaimEvent().isVetoed()) return;
        boolean fascist = legislativeSession.getClaimEvent().getPlayedPolicy() == Claim.FASCIST;

        if(removed && fascist) {
            fascistPolicies--;
        } else if (removed) {
            liberalPolicies--;
        } else if (fascist) {
            fascistPolicies++;

            if (fascistPolicies == gameTrack.getFasPolicies()) {
                ((MainActivity) c).displayEndGameOptions();
            } else addTrackAction(legislativeSession.getVoteEvent().getPresidentName());

        } else {
            liberalPolicies++;

            if(liberalPolicies == gameTrack.getLibPolicies()) {
                ((MainActivity) c).displayEndGameOptions();
            }
        }

    }

    private static void addTrackAction(String presidentName) {
        switch (gameTrack.getActions()[fascistPolicies - 1]) {
            case FascistTrack.NO_POWER:
                break;
            case FascistTrack.DECK_PEEK:
                addEvent(new PolicyPeekEvent(presidentName, c));
                break;
            case FascistTrack.EXECUTION:
                addEvent(new ExecutionEvent(presidentName, c));
                break;
            case FascistTrack.INVESTIGATION:
                addEvent(new LoyaltyInvestigationEvent(presidentName, c));
                break;
            case FascistTrack.SPECIAL_ELECTION:
                addEvent(new SpecialElectionEvent(presidentName, c));
        }
    }

    public static void reSetSessionNumber() {
        /*
        When deleting Events, the numbers of the legislative sessions would not be correct. (e.g. if I have three sessions and I delete session 2 I want session 3 to turn into a 2) That's what this function is for
         */
        int currentSessionNumber = 1;
        for(int i = 0; i < eventList.size(); i++) {
            GameEvent event = eventList.get(i);
            if(event.getClass() == LegislativeSession.class) {//it is a legislative session
                ((LegislativeSession) event).setSessionNumber(currentSessionNumber++); //Change the session number
                cardListAdapter.notifyItemChanged(i); //Update the UI
            }
        }
        legSessionNo = currentSessionNumber; //Update the global variable
    }

    public static JSONArray getEventsJSON() {
        return arr;
    }

    public static void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final GameEvent event = eventList.get(position);

                remove(event);
                Snackbar snackbar = Snackbar.make(cardList, c.getString(R.string.snackbar_GameEvent_removed_message), BaseTransientBottomBar.LENGTH_LONG);

                snackbar.setAction(c.getString(R.string.undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        undoRemoval(event, position);
                    }
                }).show();

                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int e) {
                        if (e == 2) {
                            JSONManager.addGameLogChange(new GameLogChange(event, GameLogChange.EVENT_DELETE));
                        }

                        super.onDismissed(transientBottomBar, e);
                    }
                });
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return swipeEnabled;
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(cardList);
    }

    private static void restoreGameFromJSON(JSONObject object) throws JSONException {
        //TODO also restore game settings
        JSONObject game = object.getJSONObject("game");
        JSONArray players = game.getJSONArray("players");
        JSONArray plays = game.getJSONArray("plays");
        arr = plays;

        for(int j = 0; j < players.length(); j++) {
            PlayerList.addPlayer(players.getString(j));
        }

        //Restore plays
        for(int i = 0; i < plays.length(); i++) {
            GameEvent event = JSONManager.createGameEventFromJSON((JSONObject) plays.get(i), c);
            eventList.add(event);
            if(event.getClass() == LegislativeSession.class) processPolicyChange((LegislativeSession) event, false);
        }
    }

    public static void backupToCache() throws IOException {
        eventListToFile(true, "backup.json");
    }

    public static boolean backupPresent() {
        return new File(c.getCacheDir(), "backup.json").exists();
    }

    public static void restoreBackup() {
        try {
            eventListFromFile(true, "backup.json");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void deleteBackup() {
        deleteFile(true, "backup.json");
    }

    public static void eventListToFile(boolean cache, String fileName) throws IOException {
        File file;
        if(cache) file = new File(c.getCacheDir(), fileName);
        else file = new File(c.getFilesDir(), fileName);

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(JSONManager.getCompleteGameJSON());
        bw.close();
    }

    public static void eventListFromFile(boolean cache, String fileName) throws JSONException {
        File file;
        if(cache) file = new File(c.getCacheDir(), fileName);
        else file = new File(c.getFilesDir(), fileName);

        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);

            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            String contents = stringBuilder.toString();
            JSONObject object = new JSONObject(contents);
            restoreGameFromJSON(object);
        }
    }

    public static void deleteFile(boolean cache, String fileName) {
        File file;
        if(cache) file = new File(c.getCacheDir(), fileName);
        else file = new File(c.getFilesDir(), fileName);

        if(file.exists()) file.delete();
    }

}

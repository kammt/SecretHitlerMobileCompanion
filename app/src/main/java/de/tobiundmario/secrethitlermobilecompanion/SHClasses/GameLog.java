package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
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

import de.tobiundmario.secrethitlermobilecompanion.CardRecyclerViewAdapter;
import de.tobiundmario.secrethitlermobilecompanion.JSONManager;
import de.tobiundmario.secrethitlermobilecompanion.MainActivity;
import de.tobiundmario.secrethitlermobilecompanion.R;

public class GameLog {

    public static int legSessionNo = 1;
    static private RecyclerView cardList;
    static List<GameEvent> eventList;
    private static boolean initialised;
    private static boolean gameStarted;

    private static RecyclerView.Adapter cardListAdapter;

    public static ArrayList<Integer> hiddenEventIndexes;

    private static JSONArray arr;
    private static Context c;

    public static int liberalPolicies = 0;
    public static int fascistPolicies = 0;

    public static boolean swipeEnabled = false;

    public static boolean executionSounds, policySounds, endSounds;

    public static boolean isInitialised() {
        return initialised;
    }

    public static boolean isGameStarted() {
        return gameStarted;
    }

    public static int getLiberalPolicies() {
        return liberalPolicies;
    }

    public static RecyclerView.Adapter getCardListAdapter() {
        return cardListAdapter;
    }

    public static void setContext(Context c) {
        GameLog.c = c;
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

        } else {

            position = eventList.size();
            try {
                arr.put(event.getJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }

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

        initialised = true;
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
        boolean fascist = legislativeSession.getClaimEvent().getPlayedPolicy() == Claim.FASCIST;

        if(removed && fascist) {
            fascistPolicies--;
        } else if (removed) {
            liberalPolicies--;
        } else if (fascist) {
            fascistPolicies++;
        } else liberalPolicies++;

        if(liberalPolicies == 4 && !removed) {
            new AlertDialog.Builder(c)
                    .setTitle(c.getString(R.string.title_end_game_policies))
                    .setMessage(c.getString(R.string.msg_end_game_policies_l))
                    .setNegativeButton(c.getString(R.string.no), null)
                    .setPositiveButton(c.getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((MainActivity) c).displayEndGameOptions();
                        }
                    })
                    .show();
        } else if (fascistPolicies == 5 && !removed) {
            new AlertDialog.Builder(c)
                    .setTitle(c.getString(R.string.title_end_game_policies))
                    .setMessage(c.getString(R.string.msg_end_game_policies_f))
                    .setNegativeButton(c.getString(R.string.no), null)
                    .setPositiveButton(c.getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((MainActivity) c).displayEndGameOptions();
                        }
                    })
                    .show();
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
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                final GameEvent event = eventList.get(position);

                remove(event);
                Snackbar.make(cardList, c.getString(R.string.snackbar_removed_message), BaseTransientBottomBar.LENGTH_LONG).setAction(c.getString(R.string.undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        undoRemoval(event, position);
                    }
                }).show();
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return swipeEnabled;
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(cardList);
    }

    private static void restoreGameFromJSON(JSONObject object) throws JSONException {
        JSONObject game = object.getJSONObject("game");
        JSONArray players = game.getJSONArray("players");
        JSONArray plays = game.getJSONArray("plays");
        arr = plays;

        for(int j = 0; j < players.length(); j++) {
            PlayerList.addPlayer(players.getString(j));
        }

        //Restore plays
        for(int i = 0; i < plays.length(); i++) {
            eventList.add(JSONManager.createGameEventFromJSON((JSONObject) plays.get(i), c));
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
        bw.write(JSONManager.getJSON());
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

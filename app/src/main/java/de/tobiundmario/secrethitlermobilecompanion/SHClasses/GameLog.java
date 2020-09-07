package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

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
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.EventCardRecyclerViewAdapter;
import de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters.ModifiedDefaultItemAnimator;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.GameEndCard;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.DeckShuffledEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutiveAction;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.PolicyPeekEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.SpecialElectionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.TopPolicyPlayedEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.VoteEvent;

public class GameLog {

    public static int legSessionNo = 1;
    static private RecyclerView cardList;
    static List<GameEvent> eventList, restoredEventList;
    private static boolean gameStarted;

    private static EventCardRecyclerViewAdapter cardListAdapter;

    public static ArrayList<Integer> hiddenEventIndexes;

    private static JSONArray arr;
    private static Context c;

    public static int liberalPolicies = 0;
    public static int fascistPolicies = 0;
    public static int electionTracker = 0;
    public static FascistTrack gameTrack = null;

    public static boolean swipeEnabled = false;

    public static boolean executionSounds, policySounds, endSounds, server;

    public static boolean isGameStarted() {
        return gameStarted;
    }

    public static int getLiberalPolicies() {
        return liberalPolicies;
    }

    public static EventCardRecyclerViewAdapter getCardListAdapter() {
        return cardListAdapter;
    }

    public static void initialise(RecyclerView recyclerView, Context context) {
        if(restoredEventList != null) {
            eventList = restoredEventList;
            restoredEventList = null;
        } else {
            eventList = new ArrayList<>();
            arr = new JSONArray();

            //Reset the policy-count
            liberalPolicies = 0;
            fascistPolicies = 0;
            electionTracker = 0;
            legSessionNo = 1;
        }
        hiddenEventIndexes = new ArrayList<>();

        c = context;

        cardList = recyclerView;
        cardListAdapter = new EventCardRecyclerViewAdapter(eventList, c);
        cardList.setAdapter(cardListAdapter);
        cardList.setItemAnimator(new ModifiedDefaultItemAnimator());
        //((SimpleItemAnimator) cardList.getItemAnimator()).setSupportsChangeAnimations(false);

        reSetSessionNumber();
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

    /**
     * This function is called by an Event in Setup mode when the FloatingActionButton is pressed, meaning that it left the Setup phase. It changes the layout of the card, processes changes made
     * because of the edit etc.
     * @param event The event that just left the setup phase
     */
    public static void notifySetupPhaseLeft(@NonNull GameEvent event) {
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

            position = eventList.indexOf(event);
            try {
                if(position != eventList.size() - 1) SharedPreferencesManager.addJSONObjectToArray(event.getJSON(), arr, position);
                else arr.put(event.getJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONManager.addGameLogChange(new GameLogChange(event, GameLogChange.NEW_EVENT));
        }

        //Nevertheless, we need to update the RecyclerViewItem
        cardListAdapter.notifyItemChanged(position);
        if(event.getClass() == LegislativeSession.class) processLegislativeSession((LegislativeSession) event, false);

        blurEventsInvolvingHiddenPlayers(PlayerList.getplayerCardRecyclerViewAdapter().getHiddenPlayers()); //Re-calling this function since a new item was added

        //Something changed - it's backup time!
        try {
            backupToCache();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Adds an event into the GameLog
     * @param event the event to be added
     */
    public static void addEvent(@NonNull GameEvent event) {
        if(event.isSetup && eventList.size() > 0 && eventList.get(eventList.size() - 1).isSetup) {
            if(event instanceof GameEndCard) {
                eventList.remove(eventList.size() - 1);
                cardListAdapter.notifyItemRemoved(eventList.size() - 1);
            } else {
                CardDialog.showMessageDialog(c, c.getString(R.string.title_warning), c.getString(R.string.dialog_message_duplicate_event_creation), c.getString(R.string.btn_ok), null, null, null);
                return;
            }
        }

        eventList.add(event);
        cardListAdapter.notifyItemInserted(eventList.size() - 1);
        try {
            if(!event.isSetup) arr.put(event.getJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(!event.isSetup && event.allInvolvedPlayersAreUnselected(PlayerList.getplayerCardRecyclerViewAdapter().getHiddenPlayers())) {
            hiddenEventIndexes.add(eventList.size() - 1);
        }

        if(event.getClass() == LegislativeSession.class && !event.isSetup) processLegislativeSession((LegislativeSession) event, false);
        if(event.isSetup) cardList.smoothScrollToPosition(eventList.size() - 1);
    }

    /**
     * Removes an Event and undoes changes made by the event e.g. un-setting a player as dead
     * @param event the removed event
     */
    public static void remove(GameEvent event) {
        if(event instanceof ExecutiveAction) {
            LegislativeSession legislativeSession = ((ExecutiveAction) event).getLinkedLegislativeSession();
            if(legislativeSession != null && eventList.indexOf(legislativeSession) != -1) {
                remove(legislativeSession);
                return;
            }
        }

        if(event instanceof TopPolicyPlayedEvent) {
            LegislativeSession legislativeSession = ((TopPolicyPlayedEvent) event).getLinkedLegislativeSession();
            if(legislativeSession != null && eventList.indexOf(legislativeSession) != -1) {
                remove(legislativeSession);
                return;
            }
        }

        int position = eventList.indexOf(event);
        if(!event.isSetup) arr.remove(position);
        eventList.remove(position);
        cardListAdapter.notifyItemRemoved(position);

        if(event instanceof ExecutionEvent && !event.isSetup) ((ExecutionEvent) event).resetOnRemoval();
        if(event instanceof LoyaltyInvestigationEvent && !event.isSetup) ((LoyaltyInvestigationEvent) event).resetOnRemoval();

        if(event instanceof LegislativeSession && !event.isSetup) {
            reSetSessionNumber();
            processLegislativeSession((LegislativeSession) event, true);

            GameEvent presidentAction = ((LegislativeSession) event).getPresidentAction();
            if(presidentAction != null) remove(presidentAction);
            if(presidentAction instanceof DeckShuffledEvent) {
                if (electionTracker == 0) { //This Legislative Session created a DeckShuffledEvent. Thus we have to reset the electionTracker integer
                    electionTracker = gameTrack.getElectionTrackerLength() - 1;
                } else electionTracker--;
            }
        }
    }

    /**
     * Re-adds the removed event. Do not use this function for adding new events!
     * @param event The prior removed event
     * @param oldPosition The position that it had previously
     */
    public static void undoRemoval(@NonNull GameEvent event, int oldPosition) {
        try {
            if(!event.isSetup) SharedPreferencesManager.addJSONObjectToArray(event.getJSON(), arr, oldPosition);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventList.add(oldPosition, event);
        blurEventsInvolvingHiddenPlayers(PlayerList.getplayerCardRecyclerViewAdapter().getHiddenPlayers()); //Re-calling this function since a new item was added

        cardListAdapter.notifyItemInserted(oldPosition);

        if(event instanceof ExecutionEvent && !event.isSetup) ((ExecutionEvent) event).undoRemoval();
        if(event instanceof LoyaltyInvestigationEvent && !event.isSetup) ((LoyaltyInvestigationEvent) event).undoRemoval();
        if(event instanceof LegislativeSession && !event.isSetup) {
            reSetSessionNumber();
            processLegislativeSession((LegislativeSession) event, false);

            GameEvent presidentAction = ((LegislativeSession) event).getPresidentAction();
            if(presidentAction != null) {
                undoRemoval(presidentAction, oldPosition + 1);
            }
        }
    }

    public static void setGameStarted(boolean isGameStarted) {
        gameStarted = isGameStarted;
        swipeEnabled = isGameStarted;
        if(isGameStarted) setupSwipeToDelete();
    }

    public static void blurEventsInvolvingHiddenPlayers(ArrayList<String> hiddenPlayers) {
        ArrayList<Integer> cardIndexesToBlur = new ArrayList<>();

        for(int i = 0; i < eventList.size(); i++) {

            if(!eventList.get(i).isSetup && eventList.get(i).allInvolvedPlayersAreUnselected(hiddenPlayers)) {
                cardIndexesToBlur.add(i);
            }
        }
        hiddenEventIndexes = cardIndexesToBlur; //Update the static ArrayList, making it accessible to the RecyclerViewAdapter. When rendering a view (which was null before), it will look up if it has to be blurred or not
        cardListAdapter.notifyDataSetChanged();
    }

    /**
     * Processes changes made by the legislative session. Its functions include:
     * - update the number of policies
     * - end the game
     * - add an action defined by the FascistTrack
     * It is also called when an event has been removed
     * @param legislativeSession the event that causes changes e.g. the added event
     * @param removed if true, the event has been removed
     */
    public static void processLegislativeSession(LegislativeSession legislativeSession, boolean removed) {
        if(legislativeSession.getVoteEvent().getVotingResult() == VoteEvent.VOTE_FAILED) {
            if(gameTrack.isManualMode()) return;

            if(removed) return;
            else {
                electionTracker++;
                if(electionTracker == gameTrack.getElectionTrackerLength()) {
                    electionTracker = 0;

                    if(gameStarted && legislativeSession.getPresidentAction() == null) {
                        TopPolicyPlayedEvent topPolicyPlayedEvent = new TopPolicyPlayedEvent(c);

                        //Link them together
                        legislativeSession.setPresidentAction(topPolicyPlayedEvent);
                        topPolicyPlayedEvent.setLinkedLegislativeSession(legislativeSession);

                        //Add it
                        addEvent(topPolicyPlayedEvent);
                    }
                }
            }
            return;
        } else electionTracker = 0;

        if(legislativeSession.getClaimEvent().isVetoed()) return;
        boolean fascist = legislativeSession.getClaimEvent().getPlayedPolicy() == Claim.FASCIST;

        if(removed && fascist) {
            fascistPolicies--;
        } else if (removed) {
            liberalPolicies--;
        } else if (fascist) {
            fascistPolicies++;

            if (fascistPolicies == gameTrack.getFasPolicies()) {
                ((MainActivity) c).fragment_game.displayEndGameOptions();
            } else if(gameStarted && legislativeSession.getPresidentAction() == null) addTrackAction(legislativeSession, false); //This method could also be called when a game is restored. In that case, we do not want to add new events

        } else {
            liberalPolicies++;

            if(liberalPolicies == gameTrack.getLibPolicies()) {
                ((MainActivity) c).fragment_game.displayEndGameOptions();
            }
        }

    }

    /**
     * Is called when a fascist policy is played. It creates an action, if necessary
     * @param session The legislative session causing this
     * @param restorationPhase If true, the track action is added while a game is being restored. It is then added to the restoredEventList ArrayList
     */
    private static void addTrackAction(LegislativeSession session, boolean restorationPhase) {
        if(gameTrack.isManualMode()) return; //If it is set to manual mode, we abort the function
        String presidentName = session.getVoteEvent().getPresidentName();

        ExecutiveAction executiveAction = null;

        switch (gameTrack.getAction(fascistPolicies - 1)) {
            case FascistTrack.NO_POWER:
                break;
            case FascistTrack.DECK_PEEK:
                executiveAction = new PolicyPeekEvent(presidentName, c);
                break;
            case FascistTrack.EXECUTION:
                executiveAction = new ExecutionEvent(presidentName, c);
                break;
            case FascistTrack.INVESTIGATION:
                executiveAction = new LoyaltyInvestigationEvent(presidentName, c);
                break;
            case FascistTrack.SPECIAL_ELECTION:
                executiveAction = new SpecialElectionEvent(presidentName, c);
        }
        if(executiveAction != null) {
            //Link both events together
            executiveAction.setLinkedLegislativeSession(session);
            session.setPresidentAction(executiveAction);
            //Add the new event
            if(restorationPhase) restoredEventList.add(executiveAction);
            else addEvent(executiveAction);
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

    /**
     * Returns all legislative sessions or all beginning at a specific session number
     * @param startingAt The first session number that should be in the ArrayList
     * @return An ArrayList containing the requested LegislativeSessions
     */
    public static ArrayList<LegislativeSession> getAllLegislativeSessions(int startingAt) {
        ArrayList<LegislativeSession> sessions = new ArrayList<>();

        for (GameEvent event : eventList) {
            if(event instanceof LegislativeSession) {
                LegislativeSession legislativeSession = (LegislativeSession) event;
                if(legislativeSession.getSessionNumber() >= startingAt) sessions.add(legislativeSession);
            }
        }

        return sessions;
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

                /*
                Firstly, check if it is allowed to be removed
                It is allowed to be removed if
                - Manual mode is enabled
                - it is the last event
                - It is the last Legislative Session in the list with no DeckShuffledEvent in front of it
                 */
                if(gameTrack.isManualMode() || position == eventList.size() - 1 || event instanceof LegislativeSession && ((LegislativeSession) event).getSessionNumber() == legSessionNo - 1 && !(eventList.get(eventList.size() - 1) instanceof DeckShuffledEvent)) {
                    remove(event);
                    Snackbar snackbar = Snackbar.make(cardList, c.getString(R.string.snackbar_GameEvent_removed_message), BaseTransientBottomBar.LENGTH_LONG);

                    snackbar.setAction(c.getString(R.string.undo), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(event instanceof ExecutiveAction) {
                                LegislativeSession legislativeSession = ((ExecutiveAction) event).getLinkedLegislativeSession();
                                if(legislativeSession != null) {
                                    undoRemoval(legislativeSession, position - 1);
                                    return;
                                }
                            } else if(event instanceof TopPolicyPlayedEvent) {
                                LegislativeSession legislativeSession = ((TopPolicyPlayedEvent) event).getLinkedLegislativeSession();
                                if(legislativeSession != null) {
                                    undoRemoval(legislativeSession, position - 1);
                                    return;
                                }
                            }
                            undoRemoval(event, position);
                        }
                    }).show();

                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int e) {
                            if (e == 2) {
                                if(event instanceof ExecutiveAction) {
                                    LegislativeSession legislativeSession = ((ExecutiveAction) event).getLinkedLegislativeSession();
                                    if(legislativeSession != null) {
                                        JSONManager.addGameLogChange(new GameLogChange(legislativeSession, GameLogChange.EVENT_DELETE));
                                        JSONManager.addGameLogChange(new GameLogChange(event, GameLogChange.EVENT_DELETE));
                                    }
                                } else if(event instanceof TopPolicyPlayedEvent) {
                                    LegislativeSession legislativeSession = ((TopPolicyPlayedEvent) event).getLinkedLegislativeSession();
                                    if(legislativeSession != null) {
                                        JSONManager.addGameLogChange(new GameLogChange(legislativeSession, GameLogChange.EVENT_DELETE));
                                        JSONManager.addGameLogChange(new GameLogChange(event, GameLogChange.EVENT_DELETE));
                                    }
                                } else if (event instanceof LegislativeSession) {
                                    GameEvent presidentAction = ((LegislativeSession) event).getPresidentAction();
                                    if(presidentAction != null && !presidentAction.isSetup) {
                                        JSONManager.addGameLogChange(new GameLogChange(event, GameLogChange.EVENT_DELETE));
                                        JSONManager.addGameLogChange(new GameLogChange(presidentAction, GameLogChange.EVENT_DELETE));
                                    }

                                } else JSONManager.addGameLogChange(new GameLogChange(event, GameLogChange.EVENT_DELETE));

                                try {
                                    backupToCache();
                                } catch (IOException | JSONException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            super.onDismissed(transientBottomBar, e);
                        }
                    });
                } else {
                    cardListAdapter.notifyItemChanged(position);
                    Toast.makeText(c, c.getString(R.string.toast_message_deletion_disabled), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return swipeEnabled;
            }

        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(cardList);
    }

    public static void setContext(Context c) {
        GameLog.c = c;
    }

    public static void backupToCache() throws IOException, JSONException {
        currentGameToFile(true, "backup.json", true);
    }

    public static boolean backupPresent() {
        return new File(c.getCacheDir(), "backup.json").exists();
    }

    public static void restoreBackup() {
        try {
            eventListFromFile(true, "backup.json");
            setGameStarted(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void deleteBackup() {
        deleteFile(true, "backup.json");
    }

    /**
     * Backs up the entire game (events, players, etc.) into a JSON format and writes it into a file
     * @param cache if true, the file will be written to cache. If false, it wil be written to the app's data directory. This is to support permanent saving of games
     * @param fileName the supplied file name (with file extension)
     * @param settings if true, the used settings (sounds, server, fascistTrack) will be written as well
     * @throws IOException
     * @throws JSONException
     */
    public static void currentGameToFile(boolean cache, String fileName, boolean settings) throws IOException, JSONException {
        File file;
        if(cache) file = new File(c.getCacheDir(), fileName);
        else file = new File(c.getFilesDir(), fileName);

        String json = JSONManager.getCompleteGameJSON();

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        if(settings) {
            JSONObject object = new JSONObject(json);
            JSONObject settingsObject = new JSONObject();

            //We begin by adding the FascistTrack
            settingsObject.put("track", JSONManager.writeFascistTrackToJSON(GameLog.gameTrack));

            //Now all other settings
            settingsObject.put("sounds_execution", GameLog.executionSounds);
            settingsObject.put("sounds_end", GameLog.endSounds);
            settingsObject.put("sounds_policy", GameLog.policySounds);

            settingsObject.put("server", GameLog.server);

            object.put("settings", settingsObject);

            bw.write(object.toString());
        } else {
            bw.write(json);
        }
        bw.close();
    }

    /**
     * Deletes a specified file
     * @param cache if true, the file will be written to cache. If false, it wil be written to the app's data directory. This is to support permanent saving of games
     * @param fileName the supplied file name (with file extension)
     */
    public static void deleteFile(boolean cache, String fileName) {
        File file;
        if(cache) file = new File(c.getCacheDir(), fileName);
        else file = new File(c.getFilesDir(), fileName);

        if(file.exists()) file.delete();
    }

    /**
     * Reads the contents from the specified file and passes it to the restoreGameFromJSON function
     * @param cache if true, the file will be written to cache. If false, it wil be written to the app's data directory. This is to support permanent saving of games
     * @param fileName the supplied file name (with file extension)
     * @throws JSONException
     */
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

    /**
     * Restores the entire game from a JSONObject
     * @param object the Object that was once written into a backup file
     * @throws JSONException
     */
    private static void restoreGameFromJSON(JSONObject object) throws JSONException {
        setGameStarted(false);
        liberalPolicies = 0;
        fascistPolicies = 0;
        electionTracker = 0;

        boolean executionSounds = false, endSounds = false, policySounds = false; //The sound settings will first be written into local variables. This is so that no sounds will be played during the event restoration as they have already been played before

        if(object.has("settings")) { //The file also included settings, they will be restored as well
            JSONObject settingsObject = object.getJSONObject("settings");

            gameTrack = JSONManager.restoreFascistTrackFromJSON(settingsObject.getJSONObject("track"));

            server = settingsObject.getBoolean("server");

            executionSounds = settingsObject.getBoolean("sounds_execution");
            endSounds = settingsObject.getBoolean("sounds_end");
            policySounds = settingsObject.getBoolean("sounds_policy");
        }

        JSONObject game = object.getJSONObject("game");
        JSONArray players = game.getJSONArray("players");
        JSONArray plays = game.getJSONArray("plays");
        arr = plays;

        //Restore players
        for(int j = 0; j < players.length(); j++) {
            PlayerList.addPlayer(players.getString(j));
        }

        //Restore plays
        restoredEventList = new ArrayList<>();
        for(int i = 0; i < plays.length(); i++) {
            GameEvent event = JSONManager.createGameEventFromJSON((JSONObject) plays.get(i), c);
            restoredEventList.add(event);
            if(event instanceof LegislativeSession) {
                processLegislativeSession((LegislativeSession) event, false);
            }

            //We have to link Legislative Session and Executive Action back manually, as this is not saved in JSON
            //To do this, we check if the event before the Executive Action / TopPolicyPlayedEvent is a Legislative Session. If so, they are linked together
            if(!gameTrack.isManualMode() && i > 0) {
                GameEvent priorEvent = restoredEventList.get(restoredEventList.size() - 2);
                if(priorEvent instanceof LegislativeSession) {
                    LegislativeSession legislativeSession = (LegislativeSession) priorEvent;

                    if (event instanceof ExecutiveAction) {
                        ((ExecutiveAction) event).setLinkedLegislativeSession(legislativeSession);
                        legislativeSession.setPresidentAction(event);
                    }

                    if (event instanceof TopPolicyPlayedEvent) {
                        ((TopPolicyPlayedEvent) event).setLinkedLegislativeSession(legislativeSession);
                        legislativeSession.setPresidentAction(event);
                    }
                }
            }
        }
        //When the auto-created executive action was not submitted (=> setup phase left) before the app closed, it will not be included in the backup. To mitigate this, we check if the last event is a LegislativeSession and if so add the track action again
        GameEvent lastEvent = restoredEventList.get(restoredEventList.size() - 1);
        if(lastEvent instanceof LegislativeSession && !gameTrack.isManualMode()) {
            addTrackAction((LegislativeSession) lastEvent, true);
        }

        //Finally, apply the settings
        GameLog.endSounds = endSounds;
        GameLog.executionSounds = executionSounds;
        GameLog.policySounds = policySounds;
    }

}

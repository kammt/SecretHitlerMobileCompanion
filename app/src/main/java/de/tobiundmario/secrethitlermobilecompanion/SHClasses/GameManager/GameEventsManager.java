package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.ExceptionHandler;
import de.tobiundmario.secrethitlermobilecompanion.MainActivity;
import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.GameEndCard;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.EventChange;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutiveAction;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.TopPolicyPlayedEvent;

import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.BackupManager.backupToCache;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.LegislativeSessionManager.legSessionNo;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.LegislativeSessionManager.processLegislativeSession;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.LegislativeSessionManager.reSetSessionNumber;

public final class GameEventsManager {
    private GameEventsManager() {}

    static List<GameEvent> eventList, restoredEventList;

    public static List<Integer> hiddenEventIndexes;

    public static JSONArray jsonData;
    private static Context c;

    public static boolean editingEnabled = true;

    public static boolean executionSounds, policySounds, endSounds, server;

    public static int startingLPolicies, startingFPolicies;

    public static void initialise(RecyclerView recyclerView, Context context) {
        if(restoredEventList != null) {
            eventList = restoredEventList;
            restoredEventList = null;
        } else {
            eventList = new ArrayList<>();
            jsonData = new JSONArray();

            //Reset the policy-count
            GameManager.liberalPolicies = 0;
            GameManager.fascistPolicies = 0;
            GameManager.electionTracker = 0;
            legSessionNo = 1;
        }
        hiddenEventIndexes = new ArrayList<>();

        c = context;
        RecyclerViewManager.initialise(recyclerView);

        reSetSessionNumber();
    }

    public static void destroy() {
        c = null;
        eventList = null;
        hiddenEventIndexes = null;
        jsonData = null;
        GameManager.gameTrack = null;
        RecyclerViewManager.destroy();
    }

    public static List<GameEvent> getEventList() {
        return eventList;
    }

    public static Context getContext() {
        return c;
    }

    /**
     * This function is called by an Event in Setup mode when the FloatingActionButton is pressed, meaning that it left the Setup phase. It changes the layout of the card, processes changes made
     * because of the edit etc.
     * @param event The event that just left the setup phase
     */
    public static void notifySetupPhaseLeft(@NonNull GameEvent event) {
        int position;

        //We have to differentiate between two separate scenarios. If the event left the Editing phase, we want to change the JSON data at a specific position. If it left setup phase, we just want to add it to the array
        position = eventList.indexOf(event);
        JSONManager.addGameLogChange(new EventChange(event, (event.isEditing) ? EventChange.EVENT_UPDATE : EventChange.NEW_EVENT));

        try {
            if(event.isEditing) {
                event.isEditing = false;
                jsonData.put(position, event.getJSON());
            } else {
                if(position != eventList.size() - 1) SharedPreferencesManager.addJSONObjectToArray(event.getJSON(), jsonData, position);
                else jsonData.put(event.getJSON());

                if(event instanceof LegislativeSession) processLegislativeSession((LegislativeSession) event, false);
            }
        } catch (JSONException e) {
            ExceptionHandler.showErrorSnackbar(e, "GameLog.notifySetupPhaseLeft() (arr.put)");
        }

        //Nevertheless, we need to update the RecyclerViewItem
        RecyclerViewManager.getCardListAdapter().notifyItemChanged(position);

        GameManager.blurEventsInvolvingHiddenPlayers(PlayerListManager.getplayerCardRecyclerViewAdapter().getHiddenPlayers()); //Re-calling this function since a new item was added

        //Something changed - it's backup time!
        try {
            backupToCache();
        } catch (IOException | JSONException e) {
            ExceptionHandler.showErrorSnackbar(e, "GameLog.notifySetupPhaseLeft() (backupToCache)");
        }
    }


    /**
     * Adds an event into the GameLog
     * @param event the event to be added
     */
    public static void addEvent(@NonNull GameEvent event) {
        if(event.isSetup && eventList.size() > 0 && eventList.get(eventList.size() - 1).isSetup && !isTrackAction(event)) { //Checking if the last event is in setup mode
            //If not, the process is blocked since we can't (or at least shouldn't) have two setups active at a time
            CardDialog.showMessageDialog(c, c.getString(R.string.title_warning), c.getString(R.string.dialog_message_duplicate_event_creation), c.getString(R.string.btn_ok), null, null, null);
            return;
        }

        eventList.add(event);
        RecyclerViewManager.getCardListAdapter().notifyItemInserted(eventList.size() - 1);
        try {
            if(!event.isSetup) jsonData.put(event.getJSON());
        } catch (JSONException e) {
            ExceptionHandler.showErrorSnackbar(e, "GameLog.addEvent() (arr.put)");
        }

        postProcessEventAdding(event);
    }

    private static void postProcessEventAdding(GameEvent event) {
        if(!event.isSetup && event.allInvolvedPlayersAreUnselected(PlayerListManager.getplayerCardRecyclerViewAdapter().getHiddenPlayers())) {
            hiddenEventIndexes.add(eventList.size() - 1);
        }

        if(event instanceof LegislativeSession && !event.isSetup) processLegislativeSession((LegislativeSession) event, false);
        if(event.isSetup) RecyclerViewManager.getCardList().smoothScrollToPosition(eventList.size() - 1);
    }

    private static boolean isTrackAction(GameEvent event) {
        return  (event instanceof ExecutiveAction && ((ExecutiveAction) event).getLinkedLegislativeSession() != null) || (event instanceof TopPolicyPlayedEvent && ((TopPolicyPlayedEvent) event).getLinkedLegislativeSession() != null);
    }

    /**
     * Removes an Event and undoes changes made by the event e.g. un-setting a player as dead
     * @param event the removed event
     */
    public static void remove(GameEvent event) {
        if(event instanceof ExecutiveAction || event instanceof TopPolicyPlayedEvent) {
            processLinkedLegislativeSessionRemoval(event);
        }

        if(event instanceof GameEndCard) ((MainActivity) c).fragment_game.undoEndGameOptions();

        int position = eventList.indexOf(event);
        if(position == -1) return;
        removeEvent(event, position);

        if(event instanceof ExecutionEvent && !event.isSetup) ((ExecutionEvent) event).resetOnRemoval();
        if(event instanceof LoyaltyInvestigationEvent && !event.isSetup) ((LoyaltyInvestigationEvent) event).resetOnRemoval();
        if(event instanceof TopPolicyPlayedEvent && !event.isSetup) ((TopPolicyPlayedEvent) event).undoChanges();

        if(event instanceof LegislativeSession && !event.isSetup) {
            processLegislativeSessionRemoval(event);
        }
    }

    private static void processLegislativeSessionRemoval(GameEvent event) {
        reSetSessionNumber();
        processLegislativeSession((LegislativeSession) event, true);

        GameEvent presidentAction = ((LegislativeSession) event).getPresidentAction();
        if(presidentAction != null) remove(presidentAction);
        if(presidentAction instanceof TopPolicyPlayedEvent) {
            if (GameManager.electionTracker == 0) { //This Legislative Session created a TopPolicyPlayedEvent. Thus we have to reset the electionTracker integer
                GameManager.electionTracker = GameManager.gameTrack.getElectionTrackerLength() - 1;
            } else GameManager.electionTracker--;
        }
    }

    private static void processLinkedLegislativeSessionRemoval(GameEvent event) {
        LegislativeSession legislativeSession = null;
        if(event instanceof ExecutiveAction) {
            legislativeSession = ((ExecutiveAction) event).getLinkedLegislativeSession();
        } else if(event instanceof TopPolicyPlayedEvent) {
            legislativeSession = ((TopPolicyPlayedEvent) event).getLinkedLegislativeSession();
        }

        if(legislativeSession != null && eventList.contains(legislativeSession)) {
            remove(legislativeSession);
        }
    }

    private static void removeEvent(GameEvent event, int position) {
        if(!event.isSetup) jsonData.remove(position);
        JSONManager.addGameLogChange(new EventChange(event, EventChange.EVENT_DELETE));
        eventList.remove(position);
        RecyclerViewManager.getCardListAdapter().notifyItemRemoved(position);
    }

    /**
     * Re-adds the removed event. Do not use this function for adding new events!
     * @param event The prior removed event
     * @param oldPosition The position that it had previously
     */
    public static void undoRemoval(@NonNull GameEvent event, int oldPosition) {
        try {
            if(!event.isSetup) SharedPreferencesManager.addJSONObjectToArray(event.getJSON(), jsonData, oldPosition);
        } catch (JSONException e) {
            ExceptionHandler.showErrorSnackbar(e, "GameLog.undoRemoval() (SharedPreferencesManager.addJSONObjectToArray)");
        }
        eventList.add(oldPosition, event);
        GameManager.blurEventsInvolvingHiddenPlayers(PlayerListManager.getplayerCardRecyclerViewAdapter().getHiddenPlayers()); //Re-calling this function since a new item was added

        RecyclerViewManager.getCardListAdapter().notifyItemInserted(oldPosition);

        if(event instanceof ExecutionEvent && !event.isSetup) ((ExecutionEvent) event).undoRemoval();
        if(event instanceof LoyaltyInvestigationEvent && !event.isSetup) ((LoyaltyInvestigationEvent) event).undoRemoval();
        if(event instanceof TopPolicyPlayedEvent) ((TopPolicyPlayedEvent) event).triggerPolicyChange(((TopPolicyPlayedEvent) event).getPolicyPlayed(), false);
        if(event instanceof LegislativeSession && !event.isSetup) {
            reSetSessionNumber();
            processLegislativeSession((LegislativeSession) event, false);

            GameEvent presidentAction = ((LegislativeSession) event).getPresidentAction();
            if(presidentAction != null) {
                undoRemoval(presidentAction, oldPosition + 1);
            }
        }
    }

    public static void setContext(Context c) { GameEventsManager.c = c; }
}
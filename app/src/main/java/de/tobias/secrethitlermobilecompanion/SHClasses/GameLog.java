package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import de.tobias.secrethitlermobilecompanion.CardRecyclerViewAdapter;
import de.tobias.secrethitlermobilecompanion.R;

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

    public static boolean executionSounds, policySounds;

    public static boolean isInitialised() {
        return initialised;
    }

    public static boolean isGameStarted() {
        return gameStarted;
    }

    public static void notifySetupPhaseLeft() {
        int position = eventList.size() - 1;
        GameEvent event = eventList.get(position);

        try {
            arr.put(event.getJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cardListAdapter.notifyItemChanged(position);
    }

    public static void remove(GameEvent event) {
        if(!event.isSetup) arr.remove(eventList.indexOf(event));
        eventList.remove(event);
        cardListAdapter.notifyItemRemoved(eventList.size());

        if(event.getClass() == LegislativeSession.class) reSetSessionNumber();
    }

    public static void undoRemoval(GameEvent event, int oldPosition) {
        try {
            arr.put(oldPosition, event.getJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventList.add(oldPosition, event);
        cardListAdapter.notifyItemInserted(oldPosition);

        if(event.getClass() == LegislativeSession.class) reSetSessionNumber();
    }

    public static void setGameStarted(boolean isGameStarted) {
        gameStarted = isGameStarted;
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

    public static JSONArray getEventsJSON() throws JSONException {
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

        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(cardList);
    }
}

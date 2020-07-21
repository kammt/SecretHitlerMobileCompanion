package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.tobias.secrethitlermobilecompanion.CardRecyclerViewAdapter;

public class GameLog {

    public static int legSessionNo = 1;
    static private RecyclerView cardList;
    static private Context c;
    static List<GameEvent> eventList = new ArrayList<GameEvent>();

    private static RecyclerView.Adapter cardListAdapter;

    public static ArrayList<Integer> hiddenEventIndexes = new ArrayList<>();

    public GameLog(RecyclerView cardList, Context context) {
        this.cardList = cardList;
        this.c = context;


        cardListAdapter = new CardRecyclerViewAdapter(eventList);
        cardList.setAdapter(cardListAdapter);
    }


    public static void addEvent(GameEvent event) {
        eventList.add(event);
        cardListAdapter.notifyItemInserted(eventList.size() - 1);
    }

    public static void blurEventsInvolvingHiddenPlayers(ArrayList<String> hiddenPlayers) {
        ArrayList<Integer> cardIndexesToBlur = new ArrayList<>();

        for(int i = 0; i < eventList.size(); i++) {
            View card = cardList.getLayoutManager().findViewByPosition(i);
            if(eventList.get(i).allInvolvedPlayersAreUnselected(hiddenPlayers)) {
                cardIndexesToBlur.add(i);
                if(card != null) card.setAlpha((float) 0.5);    //When the Card is not in view, the view returned will be null. Hence, we have to check
            } else if (card != null && card.getAlpha() < 1) {   //If it shouldn't be blurred, we have to check if it is in view (not null) and is still blurred. If so, we have to un-blur it
                card.setAlpha(1);
            }
        }
        hiddenEventIndexes = cardIndexesToBlur; //Update the static ArrayList, making it accessible to the RecyclerViewAdapter. When rendering a view, it will look up if it has to be blurred or not
    }


    public static GameEvent[] getAllEvents() {
        return (GameEvent[]) eventList.toArray();
    }
}

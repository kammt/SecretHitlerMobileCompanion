package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.tobias.secrethitlermobilecompanion.CardRecyclerViewAdapter;

public class GameLog {

    public static int legSessionNo = 1;
    private RecyclerView cardList;
    private Context c;
    List<GameEvent> eventList = new ArrayList<GameEvent>();

    private RecyclerView.Adapter cardListAdapter;



    public GameLog(RecyclerView cardList, Context context) {
        this.cardList = cardList;
        this.c = context;


        cardListAdapter = new CardRecyclerViewAdapter(eventList);
        cardList.setAdapter(cardListAdapter);
    }


    public void addEvent(GameEvent event) {
        eventList.add(event);
    }



    public GameEvent[] getAllEvents() {
        return (GameEvent[]) eventList.toArray();
    }
}

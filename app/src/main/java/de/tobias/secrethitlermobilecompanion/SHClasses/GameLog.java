package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.tobias.secrethitlermobilecompanion.MainActivity;
import de.tobias.secrethitlermobilecompanion.RecyclerViewAdapter;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class GameLog {

    public static int legSessionNo = 1;
    private RecyclerView cardList;
    private Context c;
    List<GameEvent> eventList = new ArrayList<GameEvent>();

    private RecyclerView.Adapter cardListAdapter;



    public GameLog(RecyclerView cardList, Context context) {
        this.cardList = cardList;
        this.c = context;


        cardListAdapter = new RecyclerViewAdapter(eventList);
        cardList.setAdapter(cardListAdapter);
    }


    public void addEvent(GameEvent event) {
        eventList.add(event);
    }



    public GameEvent[] getAllEvents() {
        return (GameEvent[]) eventList.toArray();
    }
}

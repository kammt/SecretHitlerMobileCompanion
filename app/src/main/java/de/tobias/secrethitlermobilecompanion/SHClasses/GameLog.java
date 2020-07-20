package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class GameLog {

    private ListView eventList;
    private ArrayAdapter<Spanned> adapter;
    private ArrayList<Spanned> listItems;

    public GameLog(ListView eventList, ArrayAdapter<Spanned> adapter, ArrayList<Spanned> listItems) {
        this.eventList = eventList;
        this.adapter = adapter;
        this.listItems = listItems;
    }

    private ArrayList<GameEvent> log = new ArrayList<GameEvent>();

    public void addEvent(GameEvent event) {
        log.add(event);
        String toAppend = event.toString();
        Spanned colored;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            colored = Html.fromHtml(toAppend,  Html.FROM_HTML_MODE_LEGACY);
        } else {
            colored = Html.fromHtml(toAppend);
        }

        adapter.add(colored);
    }



    public GameEvent[] getAllEvents() {
        return (GameEvent[]) log.toArray();
    }
}

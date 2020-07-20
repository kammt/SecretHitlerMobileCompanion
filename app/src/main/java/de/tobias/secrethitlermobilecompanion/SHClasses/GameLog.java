package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.MainActivity;

public class GameLog {

    private LinearLayout eventList;
    private ArrayList<TextView> listItems = new ArrayList<TextView>();
    private Context c;
    private LinearLayout.LayoutParams layoutParams =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);



    public GameLog(LinearLayout eventList, Context context) {
        this.eventList = eventList;
        this.c = context;
        layoutParams.setMargins(0, 4, 0, 4);
        eventList.setLayoutParams(layoutParams);
    }

    private ArrayList<GameEvent> log = new ArrayList<GameEvent>();

    public void addEvent(GameEvent event) {
        log.add(event);
        String toAppend = event.toString();
        Spanned colored;

        TextView listItem = new TextView(c);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            listItem.setText(Html.fromHtml(toAppend,  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        } else {
            listItem.setText(Html.fromHtml(toAppend), TextView.BufferType.SPANNABLE);
        }

        listItems.add(listItem);
        eventList.addView(listItem);
    }



    public GameEvent[] getAllEvents() {
        return (GameEvent[]) log.toArray();
    }
}

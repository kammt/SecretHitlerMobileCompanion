package de.tobiundmario.secrethitlermobilecompanion.SpinnerAdapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrack;

public class TrackActionSpinnerAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private static ArrayList<String> titles, descriptions;
    private static ArrayList<Drawable> icons;
    Context c;

    private static void setupArrayLists(Context c) {
        titles = new ArrayList<>();
        descriptions = new ArrayList<>();
        icons = new ArrayList<>();

        //No Action
        titles.add(c.getString(R.string.title_noAction));
        descriptions.add(c.getString(R.string.description_noAction));
        icons.add(ContextCompat.getDrawable(c, R.drawable.ic_cancel));

        //Execution
        titles.add(c.getString(R.string.title_execution));
        descriptions.add(c.getString(R.string.description_execution));
        icons.add(ContextCompat.getDrawable(c, R.drawable.execution));

        //Policy Peek
        titles.add(c.getString(R.string.title_policyPeek));
        descriptions.add(c.getString(R.string.description_policyPeek));
        icons.add(ContextCompat.getDrawable(c, R.drawable.policy_peek));

        //Investigation
        titles.add(c.getString(R.string.title_investigation));
        descriptions.add(c.getString(R.string.description_investigation));
        icons.add(ContextCompat.getDrawable(c, R.drawable.investigate_loyalty));

        //Special Election
        titles.add(c.getString(R.string.title_special_election));
        descriptions.add(c.getString(R.string.description_special_election));
        icons.add(ContextCompat.getDrawable(c, R.drawable.special_election));
    }

    public TrackActionSpinnerAdapter(Context c) {
        this.c = c;
        inflater = LayoutInflater.from(c);

        //Set up the lists for the spinners
        if(titles == null) TrackActionSpinnerAdapter.setupArrayLists(c);
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public Object getItem(int position) {
        switch (position) {
            case 0:
                return FascistTrack.NO_POWER;
            case 1:
                return FascistTrack.EXECUTION;
            case 2:
                return FascistTrack.DECK_PEEK;
            case 3:
                return FascistTrack.INVESTIGATION;
            case 4:
                return FascistTrack.SPECIAL_ELECTION;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, parent);
    }

    private View createView(int position, ViewGroup parent) {
        View view = inflater.inflate(R.layout.spinner_item_track_action, parent, false);

        ((TextView) view.findViewById(R.id.title)).setText(titles.get(position));
        ((TextView) view.findViewById(R.id.description)).setText(descriptions.get(position));
        ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(icons.get(position));

        return view;
    }
}

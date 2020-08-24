package de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrack;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.JSONManager;

public class CustomTracksRecyclerViewAdapter extends RecyclerView.Adapter<CustomTracksRecyclerViewAdapter.TrackViewHolder> {

    public JSONArray tracks;
    Context context;

    public CustomTracksRecyclerViewAdapter(JSONArray tracks, Context context){
        this.tracks = tracks;
        this.context = context;
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder {
        CardView cv;

        TrackViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView;
        }
    }

    @Override
    public int getItemCount() {
        return tracks.length();
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_fascist_track, viewGroup, false);

        TrackViewHolder cardViewHolder = new TrackViewHolder(v);
        return cardViewHolder;
    }

    @Override
    public void onBindViewHolder(TrackViewHolder trackViewHolder, final int pos) {
        try {
            JSONObject object = tracks.getJSONObject(pos);
            FascistTrack track = JSONManager.restoreFascistTrackFromJSON(object);

            boolean manual = track.isManualMode();

            CardView cv = trackViewHolder.cv;

            TextView tv_name = cv.findViewById(R.id.tv_trackName);
            TextView tv_desc = cv.findViewById(R.id.tv_description_policies);
            LinearLayout ll_actions = cv.findViewById(R.id.ll_actionSymbols);
            Button btn_use = cv.findViewById(R.id.btn_use);

            ll_actions.removeAllViews();
            tv_name.setText(track.getName());
            if(manual) {
                tv_desc.setText(context.getString(R.string.description_track_manual, track.getFasPolicies(), track.getLibPolicies()));
                ll_actions.setVisibility(View.GONE);
            } else {
                tv_desc.setText(context.getString(R.string.description_track, track.getFasPolicies(), track.getLibPolicies()));
                ll_actions.setVisibility(View.VISIBLE);

                int width_and_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width_and_height, width_and_height);
                params.rightMargin = 40;

                int[] actions = track.getActions();
                for(int i = 0; i < actions.length; i++) {
                    int action = actions[i];

                    if(i == actions.length - 1) params.rightMargin = 0;

                    ImageView icon_action = new ImageView(context);
                    icon_action.setLayoutParams(params);

                    switch (action) {
                        case FascistTrack.NO_POWER:
                            icon_action.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_cancel_24));
                            break;
                        case FascistTrack.DECK_PEEK:
                            icon_action.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.policy_peek));
                            break;
                        case FascistTrack.EXECUTION:
                            icon_action.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.execution));
                            break;
                        case  FascistTrack.INVESTIGATION:
                            icon_action.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.investigate_loyalty));
                            break;
                        case FascistTrack.SPECIAL_ELECTION:
                            icon_action.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.special_election));
                    }

                    ll_actions.addView(icon_action);
                }
            }

            btn_use.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}


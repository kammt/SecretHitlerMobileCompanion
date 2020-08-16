package de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OldPlayerListRecyclerViewAdapter extends RecyclerView.Adapter<OldPlayerListRecyclerViewAdapter.OldPlayerListViewHolder> {

    JSONArray oldPlayers;
    Context context;

    public OldPlayerListRecyclerViewAdapter(JSONArray oldPlayers, Context context){
        this.oldPlayers = oldPlayers;
        this.context = context;
    }

    public static class OldPlayerListViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rv;
        PlayerRecyclerViewAdapter adapter;
        ArrayList<String> players;

        OldPlayerListViewHolder(View itemView) {
            super(itemView);
            rv = (RecyclerView) itemView;
        }
    }

    @Override
    public int getItemCount() {
        return oldPlayers.length();
    }

    @Override
    public OldPlayerListViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 30;
        recyclerView.setLayoutParams(params);

        OldPlayerListViewHolder cardViewHolder = new OldPlayerListViewHolder(recyclerView);
        return cardViewHolder;
    }

    @Override
    public void onBindViewHolder(OldPlayerListViewHolder oldPlayerListViewHolder, int pos) {
        try {
            JSONObject object = oldPlayers.getJSONObject(pos);
            final ArrayList<String> players = new ArrayList<>();

            for (int j = 0; j < object.length(); j++) {
                players.add( object.getString("" + j) );
            }
            oldPlayerListViewHolder.players = players;

            RecyclerView recyclerView = oldPlayerListViewHolder.rv;
            oldPlayerListViewHolder.adapter = new PlayerRecyclerViewAdapter(players, context, true);
            recyclerView.setAdapter(oldPlayerListViewHolder.adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}


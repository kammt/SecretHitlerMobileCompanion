package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.R;

public class DeckShuffledEvent extends GameEvent {

    private int liberalPolicies, fascistPolicies;

    public DeckShuffledEvent(int liberalPolicies, int fascistPolicies) {
        this.fascistPolicies = fascistPolicies;
        this.liberalPolicies = liberalPolicies;
    }

    @Override
    public void setupCard(CardView cardView) {
        TextView tvliberal = cardView.findViewById(R.id.tv_lpolicies);
        tvliberal.setText("" + liberalPolicies);

        TextView tvfascist = cardView.findViewById(R.id.tv_fpolicies);
        tvfascist.setText("" + fascistPolicies);
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return false; //as no players are involved anyway
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("type", "shuffle");
        obj.put("fascist_policies", fascistPolicies);
        obj.put("liberal_policies", liberalPolicies);

        return obj;
    }
}

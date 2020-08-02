package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.R;

public class DeckShuffledEvent extends GameEvent {

    private int liberalPolicies, fascistPolicies;
    private Context context;

    public DeckShuffledEvent(int liberalPolicies, int fascistPolicies, Context context, boolean setup) {
        this.fascistPolicies = fascistPolicies;
        this.liberalPolicies = liberalPolicies;
        isSetup = setup;
    }

    @Override
    public void setupSetupCard(CardView cardView) {
        FloatingActionButton fab_create = cardView.findViewById(R.id.fab_create);
        final EditText et_liberalp = cardView.findViewById(R.id.et_lpolicies);
        final EditText et_fascistp = cardView.findViewById(R.id.et_fpolicies);

        fab_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean failed = false;

                if(et_liberalp.getText().toString().equals("")) {
                    et_liberalp.setError(context.getString(R.string.cannot_be_empty));
                    failed = true;
                }

                if(et_fascistp.getText().toString().equals("")) {
                    et_fascistp.setError(context.getString(R.string.cannot_be_empty));
                    failed = true;
                }

                if(!failed) {
                    liberalPolicies = Integer.parseInt(et_liberalp.getText().toString());
                    fascistPolicies = Integer.parseInt(et_fascistp.getText().toString());
                    isSetup = false;
                    GameLog.notifySetupPhaseLeft(DeckShuffledEvent.this);
                }
            }
        });
    }

    @Override
    public void setupEditCard(CardView cardView) {
        EditText et_liberalp = cardView.findViewById(R.id.et_lpolicies);
        EditText et_fascistp = cardView.findViewById(R.id.et_fpolicies);

        et_liberalp.setText("" + liberalPolicies);
        et_fascistp.setText("" + fascistPolicies);
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

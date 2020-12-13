package de.tobiundmario.secrethitlermobilecompanion.SHEvents;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;

public class DeckShuffledEvent extends GameEvent {

    private int liberalPolicies, fascistPolicies;
    private Context context;

    public DeckShuffledEvent(int liberalPolicies, int fascistPolicies, Context context) {
        this.fascistPolicies = fascistPolicies;
        this.liberalPolicies = liberalPolicies;
        this.context = context;
    }

    public DeckShuffledEvent(Context context) {
        isSetup = true;
        this.context = context;
    }

    @Override
    public void initialiseSetupCard(final CardView cardView) {
        Button btn_create = cardView.findViewById(R.id.btn_setup_forward);
        final EditText et_liberalp = cardView.findViewById(R.id.et_lpolicies);
        final EditText et_fascistp = cardView.findViewById(R.id.et_fpolicies);

        btn_create.setOnClickListener(new View.OnClickListener() {
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

                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(cardView.getWindowToken(), 0);

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
    public void setCurrentValues(CardView cardView) {
        EditText et_liberalp = cardView.findViewById(R.id.et_lpolicies);
        EditText et_fascistp = cardView.findViewById(R.id.et_fpolicies);

        et_liberalp.setText("" + liberalPolicies);
        et_fascistp.setText("" + fascistPolicies);
    }

    @Override
    public void initialiseCard(CardView cardView) {
        TextView tvliberal = cardView.findViewById(R.id.tv_lpolicies);
        tvliberal.setText("" + liberalPolicies);

        TextView tvfascist = cardView.findViewById(R.id.tv_fpolicies);
        tvfascist.setText("" + fascistPolicies);

        /* We now check if there are inconsistencies concerning the claims made and the actual amount of policies we have
            To do this, we take all legislative sessions before this event and count the policies that were drawn by the president (subtracting the played policy of course)
            If this number doesn't match with the shuffled deck, we display a warning
         */
        LinearLayout ll_warningMessage = cardView.findViewById(R.id.warning_mismatching_deck);

        ArrayList<LegislativeSession> legSessions = new ArrayList<>();
        DeckShuffledEvent lastShuffle = null;
        List<GameEvent> eventList = GameLog.getEventList();

        int position = eventList.indexOf(DeckShuffledEvent.this);

        for (int i = position - 1; i >= 0; i--) {
            GameEvent event = eventList.get(i);

            if(event instanceof DeckShuffledEvent) {
                lastShuffle = (DeckShuffledEvent) event;
                break;
            }
            else if(event instanceof LegislativeSession) {
                legSessions.add((LegislativeSession) event);
            }
        }

        if(lastShuffle == null) lastShuffle = new DeckShuffledEvent(11, 7, context);

        int fascist = lastShuffle.fascistPolicies, liberal = lastShuffle.liberalPolicies;
        for(LegislativeSession legislativeSession : legSessions) {

            ClaimEvent claimEvent = legislativeSession.getClaimEvent();
            if(claimEvent != null) {
                int presClaim = claimEvent.getPresidentClaim();

                switch (presClaim) {
                    case Claim.BBB:
                        liberal = liberal - 3;
                        break;
                    case Claim.RBB:
                        liberal = liberal - 2;
                        fascist = fascist - 1;
                        break;
                    case Claim.RRB:
                        liberal = liberal - 1;
                        fascist = fascist - 2;
                        break;
                    case Claim.RRR:
                        fascist = fascist - 3;
                }
            }

        }

        if(liberal != liberalPolicies || fascist != fascistPolicies) {
            ll_warningMessage.setVisibility(View.VISIBLE);
        } else ll_warningMessage.setVisibility(View.GONE);
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return false; //as no players are involved anyway
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("id", id);
        obj.put("type", "shuffle");
        obj.put("fascist_policies", fascistPolicies);
        obj.put("liberal_policies", liberalPolicies);

        return obj;
    }
}

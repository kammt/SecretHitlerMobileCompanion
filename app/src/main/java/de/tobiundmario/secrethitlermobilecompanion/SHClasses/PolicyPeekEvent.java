package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.R;

import static de.tobiundmario.secrethitlermobilecompanion.CardSetupHelper.getClaimAdapter;
import static de.tobiundmario.secrethitlermobilecompanion.CardSetupHelper.getPlayerNameAdapter;

public class PolicyPeekEvent extends ExecutiveAction {

    private Context c;
    private String presidentName;
    private int claim;

    public PolicyPeekEvent(String presidentName, int claim, Context context, boolean setup) {
        this.presidentName = presidentName;
        this.claim = claim;
        this.c = context;
        isSetup = setup;
    }

    @Override
    public String getInfoText() {
        return c.getString(R.string.policypeek_string, PlayerList.boldPlayerName(presidentName), Claim.getClaimString(c, claim));
    }

    @Override
    public Drawable getDrawable() {
        return c.getDrawable(R.drawable.policy_peek);
    }

    @Override
    public void setupSetupCard(CardView cardView) {
        FloatingActionButton fab_create = cardView.findViewById(R.id.fab_create);

        final Spinner presSpinner = cardView.findViewById(R.id.spinner_president);
        ArrayAdapter<String> playerListadapter = getPlayerNameAdapter(c);
        playerListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presSpinner.setAdapter(playerListadapter);

        final Spinner presClaimSpinner = cardView.findViewById(R.id.spinner_pres_claim);
        final ArrayAdapter<String> presClaimListadapter = getClaimAdapter(c, Claim.getPresidentClaims());
        presClaimListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presClaimSpinner.setAdapter(presClaimListadapter);

        fab_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presidentName = presSpinner.getSelectedItem().toString();
                claim = Claim.getClaimInt(presClaimSpinner.getSelectedItem().toString());
                isSetup = false;
                GameLog.notifySetupPhaseLeft(PolicyPeekEvent.this);
            }
        });
    }

    @Override
    public void setCurrentValues(CardView cardView) {
        Spinner presSpinner = cardView.findViewById(R.id.spinner_president);
        Spinner presClaimSpinner = cardView.findViewById(R.id.spinner_pres_claim);

        presClaimSpinner.setSelection( Claim.getPresidentClaims().indexOf( Claim.getClaimStringForJSON(c, claim)) );
        presSpinner.setSelection(PlayerList.getPlayerPosition( presidentName ));
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return unselectedPlayers.contains(presidentName);
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("type", "executive-action");
        obj.put("executive_action_type", "policy_peek");
        obj.put("president", presidentName);
        obj.put("claim", Claim.getClaimStringForJSON(c, claim));

        return obj;
    }
}

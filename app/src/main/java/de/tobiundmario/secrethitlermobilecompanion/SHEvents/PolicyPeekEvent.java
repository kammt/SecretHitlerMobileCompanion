package de.tobiundmario.secrethitlermobilecompanion.SHEvents;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.cardview.widget.CardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardSetupHelper;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;

import static de.tobiundmario.secrethitlermobilecompanion.SHCards.CardSetupHelper.getClaimAdapter;
import static de.tobiundmario.secrethitlermobilecompanion.SHCards.CardSetupHelper.getPlayerNameAdapter;

public class PolicyPeekEvent extends ExecutiveAction {

    private Context c;
    private String presidentName;
    private int claim;

    public PolicyPeekEvent(String presidentName, int claim, Context context) {
        this.presidentName = presidentName;
        this.claim = claim;
        this.c = context;
    }

    public PolicyPeekEvent(String presidentName, Context context) {
        this.c = context;
        this.presidentName = presidentName;
        isSetup = true;
    }

    @Override
    public String getInfoText() {
        return c.getString(R.string.policypeek_string, PlayerListManager.boldPlayerName(presidentName), Claim.getClaimString(c, claim));
    }

    @Override
    public Drawable getDrawable() {
        return c.getDrawable(R.drawable.policy_peek);
    }

    @Override
    public void initialiseSetupCard(CardView cardView) {
        Button btn_create = cardView.findViewById(R.id.btn_setup_forward);

        final Spinner presSpinner = cardView.findViewById(R.id.spinner_president);
        ArrayAdapter<String> playerListadapter = getPlayerNameAdapter(c);
        playerListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presSpinner.setAdapter(playerListadapter);

        if(presidentName != null) CardSetupHelper.lockPresidentSpinner(presidentName, presSpinner);

        final Spinner presClaimSpinner = cardView.findViewById(R.id.spinner_pres_claim);
        final ArrayAdapter<String> presClaimListadapter = getClaimAdapter(c, Claim.getPresidentClaims());
        presClaimListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presClaimSpinner.setAdapter(presClaimListadapter);

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presidentName = presSpinner.getSelectedItem().toString();
                claim = Claim.getClaimInt(presClaimSpinner.getSelectedItem().toString());
                isSetup = false;
                GameEventsManager.notifySetupPhaseLeft(PolicyPeekEvent.this);
            }
        });
    }

    @Override
    public void setCurrentValues(CardView cardView) {
        Spinner presSpinner = cardView.findViewById(R.id.spinner_president);
        Spinner presClaimSpinner = cardView.findViewById(R.id.spinner_pres_claim);

        presClaimSpinner.setSelection( Claim.getPresidentClaims().indexOf( Claim.getClaimStringForJSON(c, claim)) );
        presSpinner.setSelection(PlayerListManager.getPlayerPosition( presidentName ));
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(List<String> unselectedPlayers) {
        return unselectedPlayers.contains(presidentName);
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("id", id);
        obj.put("type", "executive-action");
        obj.put("executive_action_type", "policy_peek");
        obj.put("president", presidentName);
        obj.put("claim", Claim.getClaimStringForJSON(c, claim));

        return obj;
    }
}

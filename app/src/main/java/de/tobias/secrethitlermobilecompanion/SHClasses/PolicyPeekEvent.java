package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.R;

import static de.tobias.secrethitlermobilecompanion.CardSetupHelper.getClaimAdapter;
import static de.tobias.secrethitlermobilecompanion.CardSetupHelper.getPlayerNameAdapter;

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
        return c.getString(R.string.policypeek_string, presidentName, Claim.getClaimString(c, claim));
    }

    @Override
    public Drawable getDrawable() {
        return c.getDrawable(R.drawable.policy_peek);
    }

    @Override
    public void setupSetupCard(CardView cardView) {
        FloatingActionButton fab_create = cardView.findViewById(R.id.fab_create);
        ImageView iv_cancel = cardView.findViewById(R.id.img_cancel);
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameLog.remove(GameLog.eventList.get(GameLog.eventList.size() - 1));
            }
        });

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
                GameLog.notifySetupPhaseLeft();
            }
        });
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

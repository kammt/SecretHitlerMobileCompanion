package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.R;

public class LoyaltyInvestigationEvent extends ExecutiveAction {

    private Context c;
    private String presidentName, playerName;
    private int claim;

    public LoyaltyInvestigationEvent(String presidentName, String playerName, int claim, Context context) {
        this.presidentName = presidentName;
        this.playerName = playerName;
        this.claim = claim;
        this.c = context;
        PlayerList.setClaim(playerName, claim, context);
    }

    @Override
    public String getInfoText() {
        return c.getString(R.string.investigation_string, presidentName, playerName, Claim.getClaimString(c, claim));
    }

    @Override
    public Drawable getDrawable() {
        return c.getDrawable(R.drawable.investigate_loyalty);
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return unselectedPlayers.contains(presidentName) && unselectedPlayers.contains(playerName);
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("type", "executive-action");
        obj.put("executive_action_type", "investigate_loyalty");
        obj.put("president", presidentName);
        obj.put("target", playerName);
        obj.put("claim", claim == Claim.LIBERAL ? 'B' : 'R');

        return obj;
    }
}

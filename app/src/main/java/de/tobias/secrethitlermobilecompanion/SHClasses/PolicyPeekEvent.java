package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.R;

public class PolicyPeekEvent extends ExecutiveAction {

    private Context c;
    private String presidentName;
    private int claim;

    public PolicyPeekEvent(String presidentName, int claim, Context context) {
        this.presidentName = presidentName;
        this.claim = claim;
        this.c = context;
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
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return unselectedPlayers.contains(presidentName);
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        String claimString = null;

        switch(claim) {
            case Claim.RRR:
                claimString = "RRR";
                break;
            case Claim.BBR:
                claimString = "BBR";
                break;
            case Claim.BRR:
                claimString = "BRR";
                break;
            case Claim.BBB:
                claimString = "BBB";
                break;
        }

        obj.put("type", "executive-action");
        obj.put("executive_action_type", "policy_peek");
        obj.put("president", presidentName);
        obj.put("target", claimString);

        return obj;
    }
}

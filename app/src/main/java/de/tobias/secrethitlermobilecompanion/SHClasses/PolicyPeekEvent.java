package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.drawable.Drawable;

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
}

package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.cardview.widget.CardView;

import de.tobias.secrethitlermobilecompanion.R;

public class ClaimEvent extends GameEvent {

    private String presidentName, chancellorName;
    private int presidentClaim, chancellorClaim;
    private int playedPolicy;
    private boolean vetoed;

    //Single Policies
    public static final int FASCIST = 1;
    public static final int LIBERAL = 0;

    //Triple Policies
    public static final int BBB = 2;
    public static final int BBR = 3;
    public static final int BRR = 4;
    public static final int RRR = 5;

    //Double Policies
    public static final int BB = 6;
    public static final int BR = 7;
    public static final int RR = 8;

    public static final int NO_CLAIM = -1;

    Context c;

    public ClaimEvent(String presidentName, String chancellorName, int presidentClaim, int chancellorClaim, int playedPolicy, boolean vetoed, Context context) {
        this.chancellorName = chancellorName;
        this.presidentName = presidentName;

        this.presidentClaim = presidentClaim;
        this.chancellorClaim = chancellorClaim;

        this.playedPolicy = playedPolicy;

        this.vetoed = vetoed;

        c = context;
    }

    public int getChancellorClaim() {
        return chancellorClaim;
    }

    public String getChancellorName() {
        return chancellorName;
    }

    public int getPlayedPolicy() {
        return playedPolicy;
    }

    public int getPresidentClaim() {
        return presidentClaim;
    }

    public String getPresidentName() {
        return presidentName;
    }

    public boolean isVetoed() {
        return vetoed;
    }

    public String getClaimString(int claimString) {
        switch(claimString){
            case BBB:
                return "<font color='blue'>BBB</font>";
            case BBR:
                return "<font color='blue'>BB</font><font color='red'>R</font>";
            case BRR:
                return "<font color='blue'>B</font><font color='red'>RR</font>";
            case RRR:
                return "<font color='red'>RRR</font>";
            case RR:
                return "<font color='red'>RR</font>";
            case BR:
                return "<font color='blue'>B</font><font color='red'>R</font>";
            case BB:
                return "<font color='blue'>BB</font>";
            default: return c.getString(R.string.claim_nothing);


        }

    }

    @Override
    public String toString() {
        String playedPolicysp;
        if(playedPolicy == LIBERAL) {
            playedPolicysp = "<font color='blue'>" + c.getString(R.string.liberal) + "</font>";
        } else {
            playedPolicysp = "<font color='red'>" + c.getString(R.string.fascist) + "</font>";
        }

        String presidentNamecolored = "<font color='grey'>" + presidentName + "</font>";
        String chancellorNamecolored = "<font color='grey'>" + chancellorName + "</font>";

        return c.getString(R.string.claim_string, playedPolicysp, presidentNamecolored, getClaimString(presidentClaim), chancellorNamecolored, getClaimString(chancellorClaim));
    }

    @Override
    public void setupCard(CardView cardLayout) {
        //Do nothing
    }

}

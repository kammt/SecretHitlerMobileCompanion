package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.util.ArrayList;
import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;

public final class Claim {
    private Claim() {}
    /*
    This Class is responsible for everything surrounding Claims and Claim values. Some of its functions do things like
    - color the Claim accordingly (red/blue)
    - provide Claim values to communicate with GameEvents
    - generate Claim values for JSON files
     */

    //Single Policies
    public static final int FASCIST = 1;
    public static final int LIBERAL = 0;

    //Triple Policies
    public static final int BBB = 2;
    public static final int RBB = 3;
    public static final int RRB = 4;
    public static final int RRR = 5;

    //Double Policies
    public static final int BB = 6;
    public static final int RB = 7;
    public static final int RR = 8;

    public static final int NO_CLAIM = -1;

    public static String getClaimString(Context context, int claimString) {
        String blueFont = "<font color='#387CB3'>";
        String redFont = "<font color='#E23A12'>";
        String closingHtml = "</font>";

        switch(claimString){
            case BBB:
                return blueFont + "BBB" + closingHtml;
            case RBB:
                return redFont + "R" + closingHtml + blueFont +"BB" + closingHtml;
            case RRB:
                return redFont + "RR" + closingHtml + blueFont + "B" + closingHtml;
            case RRR:
                return redFont + "RRR" + closingHtml;
            case RR:
                return redFont + "RR" + closingHtml;
            case RB:
                return redFont + "R" + closingHtml + blueFont + "B" + closingHtml;
            case BB:
                return blueFont + "BB" + closingHtml;
            case LIBERAL:
                return blueFont + context.getString(R.string.liberal) + closingHtml;
            case FASCIST:
                return redFont + context.getString(R.string.fascist) + closingHtml;
            default: return context.getString(R.string.claim_nothing); //Either no claim was made or the value is invalid
        }
    }

    public static String getClaimStringForJSON(Context context, int claimString) {
        switch(claimString){
            case BBB:
                return "BBB";
            case RBB:
                return "RBB";
            case RRB:
                return "RRB";
            case RRR:
                return "RRR";
            case RR:
                return "RR";
            case RB:
                return "RB";
            case BB:
                return "BB";
            case LIBERAL:
                return "B";
            case FASCIST:
                return "R";
            default: return context.getString(R.string.claim_nothing); //Either no claim was made or the value is invalid
        }
    }

    /**
     * Checks if all claims specified in an Legislative session fit together
     * @param presidentClaim The president's claim
     * @param chancellorClaim The chancellor's claim
     * @param playedPolicy The played policy
     * @return if true the claims match together, false if otherwise
     */
    public static boolean doClaimsFit(int presidentClaim, int chancellorClaim, int playedPolicy) {
        if (presidentClaim == RRR && chancellorClaim == RR && playedPolicy == FASCIST) return true;

        else if (presidentClaim == RRB && chancellorClaim == RR && playedPolicy == FASCIST) return true;
        else if (presidentClaim == RRB && chancellorClaim == RB) return true; //It doesn't matter what policy is played, not checking

        else if (presidentClaim == RBB && chancellorClaim == BB && playedPolicy == LIBERAL) return true;
        else if (presidentClaim == RBB && chancellorClaim == RB) return true; //It doesn't matter what policy is played, not checking

        else return presidentClaim == BBB && chancellorClaim == BB && playedPolicy == LIBERAL;
    }

    public static List<String> getPresidentClaims() {
        ArrayList<String> claims = new ArrayList<>();

        claims.add("RRR");
        claims.add("RRB");
        claims.add("RBB");
        claims.add("BBB");

        return claims;
    }

    public static List<String> getChancellorClaims() {
        ArrayList<String> claims = new ArrayList<>();

        claims.add("RR");
        claims.add("RB");
        claims.add("BB");

        return claims;
    }

    public static Spanned colorClaim(String claim) {
        String liberalAndFascistReplace = claim.replace("B","<font color='#387CB3'>" + "B" + "</font>").replace("R","<font color='#E23A12'>" + "R" + "</font>");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(liberalAndFascistReplace,  Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(liberalAndFascistReplace);
        }
    }

    /**
     * Is used in conjunction with game restoration, as the claims are stored as strings in JSON files (See getClaimStringForJSON() )
     * @param claimString the string that was saved in JSON
     * @return the claim as an Integer value
     */
    public static Integer getClaimInt(String claimString) {
        switch(claimString){
            case "BBB":
                return BBB;
            case "RBB":
                return RBB;
            case "RRB":
                return RRB;
            case "RRR":
                return RRR;
            case "RR":
                return RR;
            case "RB":
                return RB;
            case "BB":
                return BB;
            case "B":
                return LIBERAL;
            case "R":
                return FASCIST;
            default: return NO_CLAIM; //Either no claim was made or the value is invalid
        }
    }
}

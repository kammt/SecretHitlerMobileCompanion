package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.R;

public class Claim {

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
        switch(claimString){
            case BBB:
                return "<font color='#387CB3'>BBB</font>";
            case RBB:
                return "<font color='red'>R</font><font color='#387CB3'>BB</font>";
            case RRB:
                return "<font color='red'>RR</font><font color='#387CB3'>B</font>";
            case RRR:
                return "<font color='#E23A12'>RRR</font>";
            case RR:
                return "<font color='#E23A12'>RR</font>";
            case RB:
                return "<font color='#E23A12'>R</font><font color='#387CB3'>B</font>";
            case BB:
                return "<font color='#387CB3'>BB</font>";
            case LIBERAL:
                return "<font color='#387CB3'>" + context.getString(R.string.liberal) + "</font>";
            case FASCIST:
                return "<font color='#E23A12'>" + context.getString(R.string.fascist) + "</font>";
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

    public static ArrayList<String> getPresidentClaims() {
        ArrayList<String> claims = new ArrayList<>();

        claims.add("RRR");
        claims.add("RRB");
        claims.add("RBB");
        claims.add("BBB");

        return claims;
    }

    public static ArrayList<String> getChancellorClaims() {
        ArrayList<String> claims = new ArrayList<>();

        claims.add("RR");
        claims.add("RB");
        claims.add("BB");

        return claims;
    }

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
            default: return NO_CLAIM; //Either no claim was made or the value is invalid
        }
    }
}

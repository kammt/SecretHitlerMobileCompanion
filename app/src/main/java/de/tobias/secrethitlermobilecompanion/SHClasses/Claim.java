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
    public static final int BBR = 3;
    public static final int BRR = 4;
    public static final int RRR = 5;

    //Double Policies
    public static final int BB = 6;
    public static final int BR = 7;
    public static final int RR = 8;

    public static final int NO_CLAIM = -1;

    public static String getClaimString(Context context, int claimString) {
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
            case LIBERAL:
                return "<font color='blue'>" + context.getString(R.string.liberal) + "</font>";
            case FASCIST:
                return "<font color='red'>" + context.getString(R.string.fascist) + "</font>";
            default: return context.getString(R.string.claim_nothing); //Either no claim was made or the value is invalid
        }
    }

    public static String getClaimStringForJSON(Context context, int claimString) {
        switch(claimString){
            case BBB:
                return "BBB";
            case BBR:
                return "BBR";
            case BRR:
                return "BRR";
            case RRR:
                return "RRR";
            case RR:
                return "RR";
            case BR:
                return "BR";
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
        claims.add("BRR");
        claims.add("BBR");
        claims.add("BBB");

        return claims;
    }

    public static ArrayList<String> getChancellorClaims() {
        ArrayList<String> claims = new ArrayList<>();

        claims.add("RR");
        claims.add("BR");
        claims.add("BB");

        return claims;
    }

    public static Integer getClaimInt(String claimString) {
        switch(claimString){
            case "BBB":
                return BBB;
            case "BBR":
                return BBR;
            case "BRR":
                return BRR;
            case "RRR":
                return RRR;
            case "RR":
                return RR;
            case "BR":
                return BR;
            case "BB":
                return BB;
            default: return NO_CLAIM; //Either no claim was made or the value is invalid
        }
    }
}
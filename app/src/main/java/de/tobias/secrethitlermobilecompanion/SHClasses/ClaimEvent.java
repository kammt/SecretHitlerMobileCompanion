package de.tobias.secrethitlermobilecompanion.SHClasses;

public class ClaimEvent extends GameEvent {

    private int presidentID, chancellorID;
    private int presidentClaim, chancellorClaim;
    private int playedPolicy;

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

    public ClaimEvent(int presidentID, int chancellorID, int presidentClaim, int chancellorClaim, int playedPolicy) {
        this.presidentID = presidentID;
        this.chancellorID = chancellorID;

        this.presidentClaim = presidentClaim;
        this.chancellorClaim = chancellorClaim;

        this.playedPolicy = playedPolicy;
    }

    public int getChancellorClaim() {
        return chancellorClaim;
    }

    public int getChancellorID() {
        return chancellorID;
    }

    public int getPlayedPolicy() {
        return playedPolicy;
    }

    public int getPresidentClaim() {
        return presidentClaim;
    }

    public int getPresidentID() {
        return presidentID;
    }
}

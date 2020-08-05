package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

public class ClaimEvent {

    private int presidentClaim, chancellorClaim;
    private int playedPolicy;
    private boolean vetoed;

    public ClaimEvent(int presidentClaim, int chancellorClaim, int playedPolicy, boolean vetoed) {
        this.presidentClaim = presidentClaim;
        this.chancellorClaim = chancellorClaim;

        this.playedPolicy = playedPolicy;

        this.vetoed = vetoed;
    }

    public int getChancellorClaim() {
        return chancellorClaim;
    }

    public int getPlayedPolicy() {
        return playedPolicy;
    }

    public int getPresidentClaim() {
        return presidentClaim;
    }

    public boolean isVetoed() {
        return vetoed;
    }


}

package de.tobias.secrethitlermobilecompanion.SHClasses;

public class VoteEvent extends GameEvent {

    private int presidentID, chancellorID;
    private int votingResult;

    public static final int VOTE_PASSED = 1;
    public static final int VOTE_FAILED = 0;

    public VoteEvent(int presidentID, int chancellorID, int votingResult) {
        this.presidentID = presidentID;
        this.chancellorID = chancellorID;

        this.votingResult = votingResult;
    }

    public int getPresidentID() {
        return presidentID;
    }

    public int getChancellorID() {
        return chancellorID;
    }

    public int getVotingResult() {
        return votingResult;
    }
}

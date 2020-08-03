package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;

public class VoteEvent {

    private String presidentName, chancellorName;
    private int votingResult;

    public static final int VOTE_PASSED = 1;
    public static final int VOTE_FAILED = 0;

    private Context c;

    public VoteEvent(String presidentName, String chancellorName, int votingResult) {
        this.presidentName = presidentName;
        this.chancellorName = chancellorName;

        this.votingResult = votingResult;
    }

    public String getPresidentName() {
        return presidentName;
    }

    public String getChancellorName() {
        return chancellorName;
    }

    public int getVotingResult() {
        return votingResult;
    }
}

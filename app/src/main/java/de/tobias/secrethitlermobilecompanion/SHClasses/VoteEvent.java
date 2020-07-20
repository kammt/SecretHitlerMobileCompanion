package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;

import de.tobias.secrethitlermobilecompanion.R;

public class VoteEvent extends GameEvent {

    private String presidentName, chancellorName;
    private int votingResult;

    public static final int VOTE_PASSED = 1;
    public static final int VOTE_FAILED = 0;

    private Context c;

    public VoteEvent(String presidentName, String chancellorName, int votingResult, Context context) {
        this.presidentName = presidentName;
        this.chancellorName = chancellorName;

        this.votingResult = votingResult;
        c = context;
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

    @Override
    public String toString() {
        String votingResultsp;
        if(votingResult == VOTE_PASSED) {
            votingResultsp = "<font color='#0e8428'>" + c.getString(R.string.vote_passed) + "</font>";
        } else {
            votingResultsp = "<font color='red'>" + c.getString(R.string.vote_rejected) + "</font>";
        }

        String presidentNamecolored = "<font color='grey'>" + presidentName + "</font>";
        String chancellorNamecolored = "<font color='grey'>" + chancellorName + "</font>";

        return c.getString(R.string.vote_string, presidentNamecolored, chancellorNamecolored, votingResultsp);
    }
}

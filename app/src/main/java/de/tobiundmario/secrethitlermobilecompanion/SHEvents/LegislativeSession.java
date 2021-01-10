package de.tobiundmario.secrethitlermobilecompanion.SHEvents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.LegislativeSessionManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.LegislativeSessionSetupManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.ServerPaneManager;

public class LegislativeSession extends GameEvent {

    /*
    To simplify creating a Legislative Session, it is divided into two sub-events: VoteEvent and ClaimEvent.
    This is so that when a vote is rejected, no ClaimEvent has to be initialised (which would involve using a lot of null objects in the constructor)
     */

    private int sessionNumber;
    private VoteEvent voteEvent;
    private ClaimEvent claimEvent;
    private Context c;
    private static ColorStateList oldcolors;

    //View items of regular CardView
    private TextView title, presName, chancName, presClaim, chancClaim, playedPolicytv;
    private ImageView playedPolicyLogo;
    private LinearLayout ll_warning_claims;

    private GameEvent presidentAction;

    private LegislativeSessionSetupManager legislativeSessionSetupManager;

    public LegislativeSession(VoteEvent voteEvent, ClaimEvent claimEvent, Context context) {
        sessionNumber = LegislativeSessionManager.legSessionNo++;
        this.voteEvent = voteEvent;
        this.claimEvent = claimEvent;

        legislativeSessionSetupManager = new LegislativeSessionSetupManager(LegislativeSession.this, context);
        c = context;
    }

    public LegislativeSession(Context context) {
        isSetup = true;
        c = context;
        legislativeSessionSetupManager = new LegislativeSessionSetupManager(LegislativeSession.this, context);
    }

    public void setPresidentAction(GameEvent presidentAction) {
        this.presidentAction = presidentAction;
    }

    public GameEvent getPresidentAction() {
        return presidentAction;
    }

    public VoteEvent getVoteEvent() {
        return voteEvent;
    }

    public void setSessionNumber(int sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public int getSessionNumber() {
        return sessionNumber;
    }

    @Override
    public void initialiseSetupCard(final CardView cardView) {
        legislativeSessionSetupManager.initialiseSetupCard(cardView);
    }

    public void leaveSetupPhase(ClaimEvent newClaimEvent, VoteEvent newVoteEvent) {
        claimEvent = newClaimEvent;
        voteEvent = newVoteEvent;

        isSetup = false;
        if(sessionNumber == 0) sessionNumber = LegislativeSessionManager.legSessionNo++;
        GameEventsManager.notifySetupPhaseLeft(LegislativeSession.this);
        playSound();
    }

    @Override
    public void setCurrentValues(CardView cardView) {
        CheckBox cb_vetoed = cardView.findViewById(R.id.checkBox_policy_vetoed);
        Switch sw_votingoutcome = cardView.findViewById(R.id.switch_vote_outcome);

        Spinner presSpinner = cardView.findViewById(R.id.spinner_president);
        Spinner chancSpinner = cardView.findViewById(R.id.spinner_chancellor);
        Spinner presClaimSpinner = cardView.findViewById(R.id.spinner_pres_claim);
        Spinner chancClaimSpinner = cardView.findViewById(R.id.spinner_chanc_claim);


        if(voteEvent.getVotingResult() == VoteEvent.VOTE_FAILED) {
            sw_votingoutcome.setChecked(true);
        } else {
            sw_votingoutcome.setChecked(false);
            if(claimEvent.getPlayedPolicy() == Claim.LIBERAL) cardView.findViewById(R.id.img_policy_liberal).performClick();
            else cardView.findViewById(R.id.img_policy_fascist).performClick();

            presClaimSpinner.setSelection( Claim.getPresidentClaims().indexOf( Claim.getClaimStringForJSON(c, claimEvent.getPresidentClaim())) );
            chancClaimSpinner.setSelection( Claim.getChancellorClaims().indexOf( Claim.getClaimStringForJSON(c, claimEvent.getChancellorClaim())) );

            if(claimEvent.isVetoed()) cb_vetoed.setChecked(true);
        }

        presSpinner.setSelection(PlayerListManager.getPlayerPosition( voteEvent.getPresidentName() ));
        chancSpinner.setSelection(PlayerListManager.getPlayerPosition( voteEvent.getChancellorName() ));
    }

    public ClaimEvent getClaimEvent() {
        return claimEvent;
    }

    private void playSound() {
        if(GameEventsManager.policySounds && voteEvent.getVotingResult() == VoteEvent.VOTE_PASSED) {
            MediaPlayer mp;
            if (claimEvent.getPlayedPolicy() == Claim.LIBERAL) mp = MediaPlayer.create(c, R.raw.enactpolicyl);
            else mp = MediaPlayer.create(c, R.raw.enactpolicyf);
            mp.start();
        }
    }

    @SuppressLint("SetTextI18n")
    public void initialiseCard(CardView cardLayout) {
        initialiseCardViewLayout(cardLayout);

        //Set the president and chancellor names. This will always be done, no matter what outcome the event was
        presName.setText(voteEvent.getPresidentName());
        chancName.setText(voteEvent.getChancellorName());

        if(voteEvent.getVotingResult() == VoteEvent.VOTE_FAILED) {
            title.setText(c.getString(R.string.legislative_session)+ " #" + sessionNumber + c.getString(R.string.rejected));

            //Hide unnecessary stuff
            hideCardViewItems(true);

            if(oldcolors == null) oldcolors =  chancName.getTextColors();
            chancName.setTextColor(Color.RED);
            chancName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            title.setText(c.getString(R.string.legislative_session)+ " #" + sessionNumber);

            //Resetting the layout
            hideCardViewItems(false);

            if(oldcolors != null) {
                chancName.setTextColor(oldcolors);
                playedPolicytv.setTextColor(oldcolors);
            }
            chancName.setPaintFlags(chancName.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            playedPolicytv.setPaintFlags(playedPolicytv.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

            //The vote didn't fail, now we have to set the colored Claims. To do this, we parse the HTML <font> attribute (see Claim.java for more)
            ServerPaneManager.setSpannable(chancClaim, Claim.getClaimString(c, claimEvent.getChancellorClaim()));
            ServerPaneManager.setSpannable(presClaim, Claim.getClaimString(c, claimEvent.getPresidentClaim()));

            Drawable playedPolicyDrawable = (claimEvent.getPlayedPolicy() == Claim.LIBERAL) ? ContextCompat.getDrawable(c, R.drawable.liberal_logo) : ContextCompat.getDrawable(c, R.drawable.fascist_logo);
            playedPolicyLogo.setImageDrawable(playedPolicyDrawable);

            if(!Claim.doClaimsFit(claimEvent.getPresidentClaim(), claimEvent.getChancellorClaim(), claimEvent.getPlayedPolicy())) {
                ll_warning_claims.setVisibility(View.VISIBLE);
            } else ll_warning_claims.setVisibility(View.GONE);
        }

        if(claimEvent != null && claimEvent.isVetoed()) {
            if(oldcolors == null) oldcolors =  playedPolicytv.getTextColors();

            title.setText(c.getString(R.string.legislative_session) + " #" + sessionNumber + c.getString(R.string.vetoed));
            playedPolicytv.setTextColor(Color.RED);
            playedPolicytv.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void initialiseCardViewLayout(CardView cardLayout) {
        title = cardLayout.findViewById(R.id.title);
        presName = cardLayout.findViewById(R.id.pres_name);
        chancName = cardLayout.findViewById(R.id.chanc_name);

        presClaim = cardLayout.findViewById(R.id.pres_claim);
        chancClaim = cardLayout.findViewById(R.id.chanc_claim);

        playedPolicytv = cardLayout.findViewById(R.id.policy_played);
        playedPolicyLogo = cardLayout.findViewById(R.id.img_policy_played);

        ll_warning_claims = cardLayout.findViewById(R.id.warning_mismatching_claims);
    }

    private void hideCardViewItems(boolean hide) {
        int visibility = hide ? View.GONE : View.VISIBLE;
        chancClaim.setVisibility(visibility);
        presClaim.setVisibility(visibility);
        playedPolicytv.setVisibility(visibility);
        playedPolicyLogo.setVisibility(visibility);
        ll_warning_claims.setVisibility(visibility);
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(List<String> unselectedPlayers) {
        return unselectedPlayers.contains(voteEvent.getPresidentName()) && unselectedPlayers.contains(voteEvent.getChancellorName());
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("id", id);
        obj.put("type", "legislative-session");
        obj.put("num", sessionNumber);
        obj.put("president", voteEvent.getPresidentName());
        obj.put("chancellor", voteEvent.getChancellorName());
        obj.put("rejected", voteEvent.getVotingResult() != VoteEvent.VOTE_PASSED);

        if(claimEvent != null) {
            obj.put("president_claim", Claim.getClaimStringForJSON(c, claimEvent.getPresidentClaim()));
            obj.put("chancellor_claim", Claim.getClaimStringForJSON(c, claimEvent.getChancellorClaim()));
            obj.put("veto", claimEvent.isVetoed());
            obj.put("policy_played", Claim.getClaimStringForJSON(c, claimEvent.getPlayedPolicy()));
        }

        return obj;
    }


}

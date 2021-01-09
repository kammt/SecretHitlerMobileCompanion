package de.tobiundmario.secrethitlermobilecompanion.SHEvents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Build;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardSetupHelper;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.OnSetupCancelledListener;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.OnSetupFinishedListener;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.SetupFinishCondition;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.LegislativeSessionManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;

public class LegislativeSession extends GameEvent {

    /*
    To simplify creating a Legislative Session, it is divided into two sub-events: VoteEvent and ClaimEvent. This is so that when a vote is rejected, no ClaimEvent has to be initialised (which would involve using a lot of null objects in the constructor)
     */

    private int sessionNumber;
    private VoteEvent voteEvent;
    private ClaimEvent claimEvent;
    private Context c;
    private static ColorStateList oldcolors;

    private GameEvent presidentAction;

    //Defining Setup variables here as they are needed in multiple setup functions
    private Spinner presSpinner, chancSpinner, presClaimSpinner, chancClaimSpinner;


    public LegislativeSession(VoteEvent voteEvent, ClaimEvent claimEvent, Context context) {
        sessionNumber = LegislativeSessionManager.legSessionNo++;
        this.voteEvent = voteEvent;
        this.claimEvent = claimEvent;
        c = context;
    }

    public LegislativeSession(Context context) {
        isSetup = true;
        c = context;
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

    public void initialiseEditCard(CardView cardView) {
        cardView.findViewById(R.id.legacy).setVisibility(View.VISIBLE);
        cardView.findViewById(R.id.initial_setup).setVisibility(View.GONE);

        //Setting up Spinners
        presSpinner = cardView.findViewById(R.id.spinner_president);
        chancSpinner = cardView.findViewById(R.id.spinner_chancellor);

        presClaimSpinner = cardView.findViewById(R.id.spinner_pres_claim);

        chancClaimSpinner = cardView.findViewById(R.id.spinner_chanc_claim);


        //Initialise all other important bits
        final LinearLayout ll_policyplayed = cardView.findViewById(R.id.ll_policy_outcome);
        final CheckBox cb_vetoed = cardView.findViewById(R.id.checkBox_policy_vetoed);
        final Switch sw_votingoutcome = cardView.findViewById(R.id.switch_vote_outcome);
        final ImageView iv_fascist = cardView.findViewById(R.id.img_policy_fascist);
        final ImageView iv_liberal = cardView.findViewById(R.id.img_policy_liberal);
        final Button btn_continue = cardView.findViewById(R.id.btn_setup_forward);

        //When the switch is changed, we want certain UI elements to disappear
        sw_votingoutcome.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ll_policyplayed.setVisibility(View.GONE);
                    cb_vetoed.setVisibility(View.GONE);
                    chancClaimSpinner.setVisibility(View.GONE);
                    presClaimSpinner.setVisibility(View.GONE);
                } else {
                    ll_policyplayed.setVisibility(View.VISIBLE);
                    cb_vetoed.setVisibility(View.VISIBLE);
                    chancClaimSpinner.setVisibility(View.VISIBLE);
                    presClaimSpinner.setVisibility(View.VISIBLE);
                }
            }
        });

        //Setting up the OnClickListeners for the ImageViews
        CardSetupHelper.setupImageViewSelector(iv_liberal, iv_fascist, ColorStateList.valueOf(c.getColor(R.color.colorLiberal)), ColorStateList.valueOf(c.getColor(R.color.colorFascist)), new View[]{cb_vetoed, sw_votingoutcome});

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processEdit(sw_votingoutcome.isChecked(), (iv_fascist.getAlpha() == (float) 1) ? Claim.FASCIST : Claim.LIBERAL, cb_vetoed.isChecked());
            }
        });
    }

    private void processEdit(boolean voteRejected, int playedPolicy, boolean vetoed) {
        final VoteEvent newVoteEvent;
        final ClaimEvent newClaimEvent;

        if (presSpinner.getSelectedItem().equals(chancSpinner.getSelectedItem())) {
            Toast.makeText(c, c.getString(R.string.err_names_cannot_be_the_same), Toast.LENGTH_LONG).show();
        } else {
            String presName = (String) presSpinner.getSelectedItem();
            String chancName = (String) chancSpinner.getSelectedItem();

            newVoteEvent = new VoteEvent(presName, chancName, voteRejected ? VoteEvent.VOTE_FAILED : VoteEvent.VOTE_PASSED);

            if (voteRejected) {
                newClaimEvent = null;
            } else {
                int presClaim = Claim.getClaimInt((String) presClaimSpinner.getSelectedItem());
                int chancClaim = Claim.getClaimInt((String) chancClaimSpinner.getSelectedItem());
                newClaimEvent = new ClaimEvent(presClaim, chancClaim, playedPolicy, vetoed);
            }

            if (isEditing) { //We are editing the card, we need to process the changes (e.g. update the policy count)
                LegislativeSessionManager.processLegislativeSessionEdit(LegislativeSession.this, claimEvent, newClaimEvent, voteEvent, newVoteEvent);
            }

            leaveSetupPhase(newClaimEvent, newVoteEvent);
            if (LegislativeSessionManager.trackActionRequired(LegislativeSession.this, claimEvent, newClaimEvent, voteEvent, newVoteEvent)) LegislativeSessionManager.addTrackAction(LegislativeSession.this, false);
        }
    }

    @Override
    public void initialiseSetupCard(final CardView cardView) {
        if(!isEditing) {
            cardView.findViewById(R.id.legacy).setVisibility(View.GONE);
            cardView.findViewById(R.id.initial_setup).setVisibility(View.VISIBLE);

            presSpinner = cardView.findViewById(R.id.spinner_president_selection);
            chancSpinner = cardView.findViewById(R.id.spinner_chancellor_selection);

            presClaimSpinner = cardView.findViewById(R.id.spinner_president_claim);
            chancClaimSpinner = cardView.findViewById(R.id.spinner_chancellor_claim);

            final ImageView icon_fascist = cardView.findViewById(R.id.icon_policyf);
            final ImageView icon_liberal = cardView.findViewById(R.id.icon_policyl);

            final ImageView icon_ja = cardView.findViewById(R.id.icon_voting_ja);
            icon_ja.setAlpha(1f);
            final ImageView icon_nein = cardView.findViewById(R.id.icon_voting_nein);
            icon_nein.setAlpha(0.2f);

            final CheckBox cb_vetoed = cardView.findViewById(R.id.checkBox_played_policy_vetoed);

            //Setting up ImageViewSelectors
            CardSetupHelper.setupImageViewSelector(icon_liberal, icon_fascist, ColorStateList.valueOf(c.getColor(R.color.colorLiberal)), ColorStateList.valueOf(c.getColor(R.color.colorFascist)), new View[]{cb_vetoed});
            CardSetupHelper.setupImageViewSelector(icon_ja, icon_nein, null, null, null);

            OnSetupFinishedListener onSetupFinishedListener = new OnSetupFinishedListener() {
                @Override
                public void onSetupFinished() {
                    boolean voteRejected = icon_nein.getAlpha() == 1.0f;
                    String presName = (String) presSpinner.getSelectedItem();
                    String chancName = (String) chancSpinner.getSelectedItem();

                    VoteEvent newVoteEvent = new VoteEvent(presName, chancName, voteRejected ? VoteEvent.VOTE_FAILED : VoteEvent.VOTE_PASSED);
                    ClaimEvent newClaimEvent;

                    if (voteRejected) {
                        newClaimEvent = null;
                    } else {
                        int presClaim = Claim.getClaimInt((String) presClaimSpinner.getSelectedItem());
                        int chancClaim = Claim.getClaimInt((String) chancClaimSpinner.getSelectedItem());

                        int playedPolicy = (icon_fascist.getAlpha() == (float) 1) ? Claim.FASCIST : Claim.LIBERAL;
                        boolean vetoed = cb_vetoed.isChecked();

                        newClaimEvent = new ClaimEvent(presClaim, chancClaim, playedPolicy, vetoed);
                    }
                    leaveSetupPhase(newClaimEvent, newVoteEvent);
                }
            };

            OnSetupCancelledListener onSetupCancelledListener = new OnSetupCancelledListener() {
                @Override
                public void onSetupCancelled() {
                    GameEventsManager.remove(LegislativeSession.this);
                }
            };

            CardSetupHelper.initialiseSetupPages(new View[]{cardView.findViewById(R.id.page1_selection), cardView.findViewById(R.id.page2_voting), cardView.findViewById(R.id.page3_policies), cardView.findViewById(R.id.page4_claims)}, (Button) cardView.findViewById(R.id.btn_setup_forward), (Button) cardView.findViewById(R.id.btn_setup_back), onSetupFinishedListener, onSetupCancelledListener, new SetupFinishCondition() {
                @Override
                public boolean shouldSetupBeFinished(int newPage) {
                    return icon_nein.getAlpha() == 1f && newPage == 3;
                }
            });
        } else {
            initialiseEditCard(cardView);
        }

        ArrayAdapter<String> playerListadapter = CardSetupHelper.getPlayerNameAdapter(c);
        playerListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presSpinner.setAdapter(playerListadapter);

        //Attempting to get the last Legislative Session and setting the next player in order as president. If this is the first LegSession, the first player will be selected
        LegislativeSession lastSession = LegislativeSessionManager.getLastLegislativeSession();
        int newChancellorPos = 1;
        if (lastSession != null) {
            int newPresidentPos = PlayerListManager.getPlayerPosition(lastSession.getVoteEvent().getPresidentName()) + 1;
            if (newPresidentPos == PlayerListManager.getPlayerList().size()) newPresidentPos = 0;

            presSpinner.setSelection(newPresidentPos);

            newChancellorPos = (newPresidentPos == PlayerListManager.getPlayerList().size() - 1) ? 0 : newPresidentPos + 1;
        }

        chancSpinner.setAdapter(playerListadapter);
        chancSpinner.setSelection(newChancellorPos); //Setting a different item on the chancellor spinner so they don't have the same name at the beginning

        final ArrayAdapter<String> presClaimListadapter = CardSetupHelper.getClaimAdapter(c, Claim.getPresidentClaims());
        presClaimListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presClaimSpinner.setAdapter(presClaimListadapter);

        ArrayAdapter<String> chancClaimListadapter = CardSetupHelper.getClaimAdapter(c, Claim.getChancellorClaims());
        chancClaimListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        chancClaimSpinner.setAdapter(chancClaimListadapter);
    }

    private void leaveSetupPhase(ClaimEvent newClaimEvent, VoteEvent newVoteEvent) {
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
        //We get the objects from the layout
        TextView title = cardLayout.findViewById(R.id.title);
        TextView presName = cardLayout.findViewById(R.id.pres_name);
        TextView chancName = cardLayout.findViewById(R.id.chanc_name);

        TextView presClaim = cardLayout.findViewById(R.id.pres_claim);
        TextView chancClaim = cardLayout.findViewById(R.id.chanc_claim);

        TextView playedPolicytv = cardLayout.findViewById(R.id.policy_played);
        ImageView playedPolicyLogo = cardLayout.findViewById(R.id.img_policy_played);

        LinearLayout ll_warning_claims = cardLayout.findViewById(R.id.warning_mismatching_claims);

        //Set the president and chancellor names. This will always be done, no matter what outcome the event was
        presName.setText(voteEvent.getPresidentName());
        chancName.setText(voteEvent.getChancellorName());

        if(voteEvent.getVotingResult() == VoteEvent.VOTE_FAILED) {
            title.setText(c.getString(R.string.legislative_session)+ " #" + sessionNumber + c.getString(R.string.rejected));

            //Hide unnecessary stuff
            chancClaim.setVisibility(View.GONE);
            presClaim.setVisibility(View.GONE);
            playedPolicytv.setVisibility(View.GONE);
            playedPolicyLogo.setVisibility(View.GONE);
            ll_warning_claims.setVisibility(View.GONE);

            if(oldcolors == null) oldcolors =  chancName.getTextColors();
            chancName.setTextColor(Color.RED);
            chancName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            title.setText(c.getString(R.string.legislative_session)+ " #" + sessionNumber);

            //Resetting the layout
            chancClaim.setVisibility(View.VISIBLE);
            presClaim.setVisibility(View.VISIBLE);
            playedPolicytv.setVisibility(View.VISIBLE);
            playedPolicyLogo.setVisibility(View.VISIBLE);

            if(oldcolors != null) {
                chancName.setTextColor(oldcolors);
                playedPolicytv.setTextColor(oldcolors);
            }
            chancName.setPaintFlags(chancName.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            playedPolicytv.setPaintFlags(playedPolicytv.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

            //The vote didn't fail, now we have to set the colored Claims. To do this, we parse the HTML <font> attribute (see Claim.java for more)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                chancClaim.setText(Html.fromHtml(Claim.getClaimString(c, claimEvent.getChancellorClaim()),  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                presClaim.setText(Html.fromHtml(Claim.getClaimString(c, claimEvent.getPresidentClaim()),  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
            } else {
                chancClaim.setText(Html.fromHtml(Claim.getClaimString(c, claimEvent.getChancellorClaim())), TextView.BufferType.SPANNABLE);
                presClaim.setText(Html.fromHtml(Claim.getClaimString(c, claimEvent.getPresidentClaim())), TextView.BufferType.SPANNABLE);
            }

            if(claimEvent.getPlayedPolicy() == Claim.LIBERAL) {
                playedPolicyLogo.setImageDrawable(c.getDrawable(R.drawable.liberal_logo));
            } else playedPolicyLogo.setImageDrawable(c.getDrawable(R.drawable.fascist_logo));

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

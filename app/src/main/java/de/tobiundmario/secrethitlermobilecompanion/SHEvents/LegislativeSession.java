package de.tobiundmario.secrethitlermobilecompanion.SHEvents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardSetupHelper;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PlayerList;

public class LegislativeSession extends GameEvent {

    /*
    To simplify creating a Legislative Session, it is divided into two sub-events: VoteEvent and ClaimEvent. This is so that when a vote is rejected, no ClaimEvent has to be initialised (which would involve using a lot of null objects in the constructor)
     */

    private int sessionNumber;
    private VoteEvent voteEvent;
    private ClaimEvent claimEvent;
    private Context c;
    private static ColorStateList oldcolors;
    private int setupPage = 1;

    private GameEvent presidentAction;

    //Defining the OnClickListeners outside of the function to call them separately in the setupEditCard() function
    private View.OnClickListener iv_fascistListener, iv_liberalListener;

    public LegislativeSession(VoteEvent voteEvent, ClaimEvent claimEvent, Context context) {
        sessionNumber = GameLog.legSessionNo++;
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

    @Override
    public void initialiseSetupCard(CardView cardView) {
        final Spinner presSpinner, chancSpinner, presClaimSpinner, chancClaimSpinner;
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
            View.OnClickListener iv_jaListener, iv_neinListener;
            final ImageView icon_nein = cardView.findViewById(R.id.icon_voting_nein);
            icon_nein.setAlpha(0.2f);

            final CheckBox cb_vetoed = cardView.findViewById(R.id.checkBox_played_policy_vetoed);

            //Changing the color scheme to liberal blue
            iv_liberalListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    icon_liberal.setAlpha((float) 1);
                    icon_fascist.setAlpha((float) 0.2);

                    ColorStateList csl = ColorStateList.valueOf(c.getColor(R.color.colorLiberal));
                    cb_vetoed.setButtonTintList(csl);
                }
            };
            icon_liberal.setOnClickListener(iv_liberalListener);

            //Changing the color scheme to fascist red
            iv_fascistListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    icon_fascist.setAlpha((float) 1);
                    icon_liberal.setAlpha((float) 0.2);

                    ColorStateList csl = ColorStateList.valueOf(c.getColor(R.color.colorFascist));
                    cb_vetoed.setButtonTintList(csl);
                }
            };
            icon_fascist.setOnClickListener(iv_fascistListener);

            iv_jaListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    icon_ja.setAlpha((float) 1);
                    icon_nein.setAlpha((float) 0.2);
                }
            };
            icon_ja.setOnClickListener(iv_jaListener);

            iv_neinListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    icon_nein.setAlpha((float) 1);
                    icon_ja.setAlpha((float) 0.2);
                }
            };
            icon_nein.setOnClickListener(iv_neinListener);

            //Initialising all pages
            final ConstraintLayout page1_selection = cardView.findViewById(R.id.page1_selection);
            final ConstraintLayout page2_voting = cardView.findViewById(R.id.page2_voting);
            final ConstraintLayout page3_policies = cardView.findViewById(R.id.page3_policies);
            final ConstraintLayout page4_claims = cardView.findViewById(R.id.page4_claims);

            page1_selection.setVisibility(View.VISIBLE);
            page2_voting.setVisibility(View.GONE);
            page3_policies.setVisibility(View.GONE);
            page4_claims.setVisibility(View.GONE);

            //Initialising buttons
            Button btn_continue = cardView.findViewById(R.id.btn_setup_forward);
            final Button btn_back = cardView.findViewById(R.id.btn_setup_back);
            btn_back.setText(c.getString(R.string.dialog_mismatching_claims_btn_cancel));

            setupPage = 1;

            btn_continue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setupPage++;
                    final ConstraintLayout oldPage, newPage;
                    switch (setupPage) {
                        case 2:
                            oldPage = page1_selection;
                            newPage = page2_voting;
                            break;
                        case 3:
                            if(icon_ja.getAlpha() == 1f) {
                                oldPage = page2_voting;
                                newPage = page3_policies;
                            } else {
                                setupPage = 5; //If the vote is rejected, then we finish the setup on page 2
                                oldPage = null;
                                newPage = null;
                            }
                            break;
                        case 4:
                            oldPage = page3_policies;
                            newPage = page4_claims;
                            break;
                        default:
                            oldPage = null;
                            newPage = null;
                    }
                    if(setupPage == 5) {
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
                    } else {
                        //Animations
                        Animation slideOutLeft = AnimationUtils.loadAnimation(c, R.anim.slide_out_left);
                        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                oldPage.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        Animation slideInRight = AnimationUtils.loadAnimation(c, R.anim.slide_in_right);
                        slideInRight.setFillAfter(true);
                        oldPage.startAnimation(slideOutLeft);

                        newPage.setVisibility(View.VISIBLE);
                        newPage.startAnimation(slideInRight);

                        if(setupPage == 2) btn_back.setText(c.getString(R.string.back));
                    }
                }
            });

            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setupPage--;
                    final ConstraintLayout oldPage, newPage;
                    switch (setupPage) {
                        case 1:
                            oldPage = page2_voting;
                            newPage = page1_selection;
                            break;
                        case 2:
                            oldPage = page3_policies;
                            newPage = page2_voting;
                            break;
                        case 3:
                            oldPage = page4_claims;
                            newPage = page3_policies;
                            break;
                        default:
                            oldPage = null;
                            newPage = null;
                    }
                    if(setupPage == 0) {
                        GameLog.remove(LegislativeSession.this);
                    } else {
                        //Animations
                        Animation slideOutRight = AnimationUtils.loadAnimation(c, R.anim.slide_out_right);
                        slideOutRight.setFillAfter(true);
                        Animation slideInLeft = AnimationUtils.loadAnimation(c, R.anim.slide_in_left);
                        slideInLeft.setFillAfter(true);
                        oldPage.startAnimation(slideOutRight);

                        newPage.setVisibility(View.VISIBLE);
                        newPage.startAnimation(slideInLeft);

                        if(setupPage == 1) btn_back.setText(c.getString(R.string.dialog_mismatching_claims_btn_cancel));
                    }
                }
            });


        } else {
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

            //Changing the color scheme to liberal blue
            iv_liberalListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv_liberal.setAlpha((float) 1);
                    iv_fascist.setAlpha((float) 0.2);

                    ColorStateList csl = ColorStateList.valueOf(c.getColor(R.color.colorLiberal));
                    cb_vetoed.setButtonTintList(csl);

                    sw_votingoutcome.setThumbTintList(csl);
                    sw_votingoutcome.setTrackTintList(csl);
                }
            };
            iv_liberal.setOnClickListener(iv_liberalListener);

            //Changing the color scheme to fascist red
            iv_fascistListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv_fascist.setAlpha((float) 1);
                    iv_liberal.setAlpha((float) 0.2);

                    ColorStateList csl = ColorStateList.valueOf(c.getColor(R.color.colorFascist));
                    cb_vetoed.setButtonTintList(csl);

                    sw_votingoutcome.setThumbTintList(csl);
                    sw_votingoutcome.setTrackTintList(csl);
                }
            };
            iv_fascist.setOnClickListener(iv_fascistListener);

            btn_continue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final VoteEvent newVoteEvent;
                    final ClaimEvent newClaimEvent;

                    if (presSpinner.getSelectedItem().equals(chancSpinner.getSelectedItem())) {

                        Toast.makeText(c, c.getString(R.string.err_names_cannot_be_the_same), Toast.LENGTH_LONG).show();

                    } else {

                        boolean addTrackAction = false; //This boolean will be triggered after a track action is needed after an edit (see code after if(isEditing))

                        boolean voteRejected = sw_votingoutcome.isChecked();
                        String presName = (String) presSpinner.getSelectedItem();
                        String chancName = (String) chancSpinner.getSelectedItem();

                        newVoteEvent = new VoteEvent(presName, chancName, voteRejected ? VoteEvent.VOTE_FAILED : VoteEvent.VOTE_PASSED);

                        if (voteRejected) {
                            newClaimEvent = null;
                        } else {
                            int presClaim = Claim.getClaimInt((String) presClaimSpinner.getSelectedItem());
                            int chancClaim = Claim.getClaimInt((String) chancClaimSpinner.getSelectedItem());

                            int playedPolicy = (iv_fascist.getAlpha() == (float) 1) ? Claim.FASCIST : Claim.LIBERAL;
                            boolean vetoed = cb_vetoed.isChecked();

                            newClaimEvent = new ClaimEvent(presClaim, chancClaim, playedPolicy, vetoed);
                        }

                        if (isEditing) { //We are editing the card, we need to process the changes (e.g. update the policy count)
                            //If we change the event to be rejected or vetoed, we reduce the policy count
                            if (newVoteEvent.getVotingResult() == VoteEvent.VOTE_FAILED || newClaimEvent != null && newClaimEvent.isVetoed()) {
                                if (claimEvent != null && claimEvent.getPlayedPolicy() == Claim.LIBERAL)
                                    GameLog.liberalPolicies--;
                                if (claimEvent != null && claimEvent.getPlayedPolicy() == Claim.FASCIST)
                                    GameLog.fascistPolicies--;
                            }

                            //If we change the event to play a policy, we increase the policy count
                            if (voteEvent.getVotingResult() == VoteEvent.VOTE_FAILED || claimEvent != null && claimEvent.isVetoed()) {
                                if (newClaimEvent != null && newClaimEvent.getPlayedPolicy() == Claim.LIBERAL)
                                    GameLog.liberalPolicies++;
                                if (newClaimEvent != null && newClaimEvent.getPlayedPolicy() == Claim.FASCIST)
                                    GameLog.fascistPolicies++;
                            }

                            //If we had a liberal policy and change it to a fascist policy, we update the policy count
                            if (newClaimEvent != null && !newClaimEvent.isVetoed() && newClaimEvent.getPlayedPolicy() == Claim.FASCIST && claimEvent != null && !claimEvent.isVetoed() && claimEvent.getPlayedPolicy() == Claim.LIBERAL) {
                                GameLog.liberalPolicies--;
                                GameLog.fascistPolicies++;
                            }

                            //If we had a fascist policy and change it to a liberal policy, we update the policy count
                            if (newClaimEvent != null && !newClaimEvent.isVetoed() && newClaimEvent.getPlayedPolicy() == Claim.LIBERAL && claimEvent != null && !claimEvent.isVetoed() && claimEvent.getPlayedPolicy() == Claim.FASCIST) {
                                GameLog.liberalPolicies++;
                                GameLog.fascistPolicies--;
                            }

                            //If we are not in manual mode, we have to recalculate the election tracker as well
                            if (!GameLog.gameTrack.isManualMode()) {
                                //If it was rejected and now not anymore, we decrease the election tracker
                                if (voteEvent.getVotingResult() == VoteEvent.VOTE_FAILED && newVoteEvent.getVotingResult() == VoteEvent.VOTE_PASSED)
                                    GameLog.electionTracker--;
                                //If it was passed and now not anymore, we increase the election tracker
                                if (voteEvent.getVotingResult() == VoteEvent.VOTE_PASSED && newVoteEvent.getVotingResult() == VoteEvent.VOTE_FAILED) {
                                    GameLog.electionTracker++;

                                    if (GameLog.electionTracker == GameLog.gameTrack.getElectionTrackerLength()) {
                                        GameLog.electionTracker = 0;

                                        TopPolicyPlayedEvent topPolicyPlayedEvent = new TopPolicyPlayedEvent(c);
                                        topPolicyPlayedEvent.setLinkedLegislativeSession(LegislativeSession.this);
                                        setPresidentAction(topPolicyPlayedEvent);

                                        GameLog.addEvent(topPolicyPlayedEvent);
                                    }
                                }
                            }

                            //If we are editing an event, this can cause changes. If there is a presidential action and we switch the policy to a liberal one, we have to remove the presidential action
                            if (getPresidentAction() != null) {
                                GameEvent presidentialAction = getPresidentAction();
                                if (voteRejected || newClaimEvent.isVetoed() || newClaimEvent.getPlayedPolicy() == Claim.LIBERAL) {
                                    if (presidentialAction instanceof ExecutiveAction)
                                        ((ExecutiveAction) presidentialAction).setLinkedLegislativeSession(null);
                                    if (presidentialAction instanceof TopPolicyPlayedEvent)
                                        ((TopPolicyPlayedEvent) presidentialAction).setLinkedLegislativeSession(null);
                                    GameLog.remove(presidentialAction);

                                    presidentAction = null;
                                }
                            }

                            //If we switch to a fascist policy, we have to create a presidential action
                            if (newVoteEvent.getVotingResult() == VoteEvent.VOTE_PASSED && newClaimEvent.getPlayedPolicy() == Claim.FASCIST && !newClaimEvent.isVetoed() && (voteEvent.getVotingResult() == VoteEvent.VOTE_FAILED || claimEvent.isVetoed() || claimEvent.getPlayedPolicy() == Claim.LIBERAL)) {
                                addTrackAction = true;
                            }

                            //If we switch the president's name and the legislative session has a presidential action, we have to change the name in that one too
                            if (!voteEvent.getPresidentName().equals(newVoteEvent.getPresidentName()) && getPresidentAction() != null) {
                                GameEvent presidentAction = getPresidentAction();
                                if (presidentAction instanceof ExecutiveAction) {
                                    ExecutiveAction presidentExecutiveAction = (ExecutiveAction) presidentAction;
                                    presidentExecutiveAction.presidentName = newVoteEvent.getPresidentName();

                                    if (presidentExecutiveAction.presidentName.equals(presidentExecutiveAction.targetName)) { //If the president and chancellor name are now the same, the user is prompted to edit that event as well
                                        presidentExecutiveAction.isSetup = true;
                                        presidentExecutiveAction.isEditing = true;
                                        GameLog.getCardListAdapter().notifyItemChanged(GameLog.getEventList().size() - 1);
                                    }
                                }
                            }

                            Log.v("LesiglativeSession Edit", "Election Tracker now at " + GameLog.electionTracker);
                            Log.v("LesiglativeSession Edit", "Liberal Policies now at " + GameLog.liberalPolicies);
                            Log.v("LesiglativeSession Edit", "Fascist policies now at " + GameLog.fascistPolicies);
                        }

                        leaveSetupPhase(newClaimEvent, newVoteEvent);
                        if (addTrackAction) GameLog.addTrackAction(LegislativeSession.this, false);
                    }
                }
            });
        }

        ArrayAdapter<String> playerListadapter = CardSetupHelper.getPlayerNameAdapter(c);
        playerListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presSpinner.setAdapter(playerListadapter);

        //Attempting to get the last Legislative Session and setting the next player in order as president. If this is the first LegSession, the first player will be selected
        LegislativeSession lastSession = GameLog.getLastLegislativeSession();
        int newChancellorPos = 1;
        if (lastSession != null) {
            int newPresidentPos = PlayerList.getPlayerPosition(lastSession.getVoteEvent().getPresidentName()) + 1;
            if (newPresidentPos == PlayerList.getPlayerList().size()) newPresidentPos = 0;

            presSpinner.setSelection(newPresidentPos);

            newChancellorPos = (newPresidentPos == PlayerList.getPlayerList().size() - 1) ? 0 : newPresidentPos + 1;
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
        if(sessionNumber == 0) sessionNumber = GameLog.legSessionNo++;
        GameLog.notifySetupPhaseLeft(LegislativeSession.this);
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
            if(claimEvent.getPlayedPolicy() == Claim.LIBERAL) iv_liberalListener.onClick(null);
            else iv_fascistListener.onClick(null);

            presClaimSpinner.setSelection( Claim.getPresidentClaims().indexOf( Claim.getClaimStringForJSON(c, claimEvent.getPresidentClaim())) );
            chancClaimSpinner.setSelection( Claim.getChancellorClaims().indexOf( Claim.getClaimStringForJSON(c, claimEvent.getChancellorClaim())) );

            if(claimEvent.isVetoed()) cb_vetoed.setChecked(true);
        }

        presSpinner.setSelection(PlayerList.getPlayerPosition( voteEvent.getPresidentName() ));
        chancSpinner.setSelection(PlayerList.getPlayerPosition( voteEvent.getChancellorName() ));
    }

    public ClaimEvent getClaimEvent() {
        return claimEvent;
    }

    private void playSound() {
        if(GameLog.policySounds && voteEvent.getVotingResult() == VoteEvent.VOTE_PASSED) {
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
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
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

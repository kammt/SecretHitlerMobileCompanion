package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Build;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.CardSetupHelper;
import de.tobias.secrethitlermobilecompanion.R;

public class LegislativeSession extends GameEvent {

    /*
    To simplify creating a Legislative Session, it is divided into two sub-events: VoteEvent and ClaimEvent. This is so that when a vote is rejected, no ClaimEvent has to be initialised (which would involve using a lot of null objects in the constructor
     */

    private int sessionNumber;
    private VoteEvent voteEvent;
    private ClaimEvent claimEvent;
    private Context c;
    private static ColorStateList oldcolors;

    public LegislativeSession(VoteEvent voteEvent, ClaimEvent claimEvent, Context context, boolean setup) {
        sessionNumber = GameLog.legSessionNo++;
        this.voteEvent = voteEvent;
        this.claimEvent = claimEvent;
        c = context;
        isSetup = setup;
    }

    public void setSessionNumber(int sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public int getSessionNumber() {
        return sessionNumber;
    }

    @Override
    public void setupSetupCard(CardView cardView) {
        //Setting up Spinners
        final Spinner presSpinner = cardView.findViewById(R.id.spinner_president);
        ArrayAdapter<String> playerListadapter = CardSetupHelper.getPlayerNameAdapter(c);
        playerListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presSpinner.setAdapter(playerListadapter);

        final Spinner chancSpinner = cardView.findViewById(R.id.spinner_chancellor);
        chancSpinner.setAdapter(playerListadapter);
        chancSpinner.setSelection(1); //Setting a different item on the chancellor spinner so they don't have the same name at the beginning

        final Spinner presClaimSpinner = cardView.findViewById(R.id.spinner_pres_claim);
        final ArrayAdapter<String> presClaimListadapter = CardSetupHelper.getClaimAdapter(c, Claim.getPresidentClaims());
        presClaimListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presClaimSpinner.setAdapter(presClaimListadapter);

        final Spinner chancClaimSpinner = cardView.findViewById(R.id.spinner_chanc_claim);
        ArrayAdapter<String> chancClaimListadapter = CardSetupHelper.getClaimAdapter(c, Claim.getChancellorClaims());
        chancClaimListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        chancClaimSpinner.setAdapter(chancClaimListadapter);


        //Initialise all other important bits
        final LinearLayout ll_policyplayed = cardView.findViewById(R.id.ll_policy_outcome);
        final CheckBox cb_vetoed = cardView.findViewById(R.id.checkBox_policy_vetoed);
        final Switch sw_votingoutcome = cardView.findViewById(R.id.switch_vote_outcome);
        final ImageView iv_fascist = cardView.findViewById(R.id.img_policy_fascist);
        final ImageView iv_liberal = cardView.findViewById(R.id.img_policy_liberal);
        final FloatingActionButton fab_create = cardView.findViewById(R.id.fab_create);
        ImageView iv_cancel = cardView.findViewById(R.id.img_cancel);

        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameLog.remove(GameLog.eventList.get(GameLog.eventList.size() - 1));
            }
        });

        //When the switch is changed, we want certain UI elements to disappear
        sw_votingoutcome.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
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
        iv_liberal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_liberal.setAlpha((float) 1);
                iv_fascist.setAlpha((float) 0.2);

                ColorStateList csl = ColorStateList.valueOf(c.getColor(R.color.colorLiberal));
                fab_create.setBackgroundTintList(csl);
                cb_vetoed.setButtonTintList(csl);

                sw_votingoutcome.setThumbTintList(csl);
                sw_votingoutcome.setTrackTintList(csl);
            }
        });

        //Changing the color scheme to fascist red
        iv_fascist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_fascist.setAlpha((float) 1);
                iv_liberal.setAlpha((float) 0.2);

                ColorStateList csl = ColorStateList.valueOf(c.getColor(R.color.colorFascist));
                fab_create.setBackgroundTintList(csl);
                cb_vetoed.setButtonTintList(csl);

                sw_votingoutcome.setThumbTintList(csl);
                sw_votingoutcome.setTrackTintList(csl);
            }
        });


        fab_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(presSpinner.getSelectedItem().equals(chancSpinner.getSelectedItem())) {

                    Toast.makeText(c, c.getString(R.string.err_names_cannot_be_the_same), Toast.LENGTH_LONG).show();

                } else {

                    boolean voteRejected = sw_votingoutcome.isChecked();
                    String presName = (String) presSpinner.getSelectedItem();
                    String chancName = (String) chancSpinner.getSelectedItem();

                    voteEvent = new VoteEvent(presName, chancName, voteRejected ? VoteEvent.VOTE_FAILED : VoteEvent.VOTE_PASSED, c);

                    if(voteRejected) {
                        claimEvent = null;
                    } else {
                        int presClaim = Claim.getClaimInt((String) presClaimSpinner.getSelectedItem());
                        int chancClaim = Claim.getClaimInt((String) chancClaimSpinner.getSelectedItem());

                        int playedPolicy = (iv_fascist.getAlpha() == (float) 1) ? Claim.FASCIST : Claim.LIBERAL;
                        boolean vetoed = cb_vetoed.isChecked();

                        claimEvent = new ClaimEvent(presName, chancName, presClaim, chancClaim, playedPolicy, vetoed, c);
                    }

                    if(claimEvent != null && !Claim.doClaimsFit(claimEvent.getPresidentClaim(), claimEvent.getChancellorClaim(), claimEvent.getPlayedPolicy())) {
                        new AlertDialog.Builder(c)
                                .setTitle(c.getString(R.string.dialog_mismatching_claims_title))
                                .setMessage(c.getString(R.string.dialog_mismatching_claims_desc))
                                .setPositiveButton(c.getString(R.string.dialog_mismatching_claims_btn_continue), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        isSetup = false;
                                        GameLog.notifySetupPhaseLeft();
                                        playSound();
                                    }
                                })
                                .setNegativeButton(c.getString(R.string.dialog_mismatching_claims_btn_cancel), null)
                                .show();
                    } else {
                        isSetup = false;
                        GameLog.notifySetupPhaseLeft();
                        playSound();
                    }

                }
            }
        });
    }

    private void playSound() {
        if(GameLog.policySounds) {
            MediaPlayer mp;
            if (claimEvent.getPlayedPolicy() == Claim.LIBERAL) mp = MediaPlayer.create(c, R.raw.enactpolicyl);
            else mp = MediaPlayer.create(c, R.raw.enactpolicyf);
            mp.start();
        }
    }

    @SuppressLint("SetTextI18n")
    public void setupCard(CardView cardLayout) {
        //We get the objects from the layout
        TextView title = cardLayout.findViewById(R.id.title);
        TextView presName = cardLayout.findViewById(R.id.pres_name);
        TextView chancName = cardLayout.findViewById(R.id.chanc_name);

        TextView presClaim = cardLayout.findViewById(R.id.pres_claim);
        TextView chancClaim = cardLayout.findViewById(R.id.chanc_claim);

        TextView playedPolicytv = cardLayout.findViewById(R.id.policy_played);
        ImageView playedPolicyLogo = cardLayout.findViewById(R.id.img_policy_played);

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

            if(oldcolors != null) chancName.setTextColor(oldcolors);
            chancName.setPaintFlags(chancName.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

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
        }

        if(claimEvent != null && claimEvent.isVetoed()) {
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

package de.tobias.secrethitlermobilecompanion;

import android.content.Context;
import android.content.res.ColorStateList;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.tobias.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobias.secrethitlermobilecompanion.SHClasses.ClaimEvent;
import de.tobias.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobias.secrethitlermobilecompanion.SHClasses.LegislativeSession;
import de.tobias.secrethitlermobilecompanion.SHClasses.PlayerList;
import de.tobias.secrethitlermobilecompanion.SHClasses.VoteEvent;

public class CardSetupHelper {
    /*
    This class is responsible for displaying the setup for each card. It is called from the onClickListeners in the MainActivity
     */

    public static void setupLegislativeSession(final Context c, LayoutInflater layoutInflater, final LinearLayout linearLayout) {
        CardView setupCard = (CardView) layoutInflater.from(c).inflate(R.layout.setup_card_legislative_session, linearLayout, false);

        //Setting up Spinners
        final Spinner presSpinner = setupCard.findViewById(R.id.spinner_president);
        ArrayAdapter<String> playerListadapter = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, PlayerList.getPlayerList());
        playerListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presSpinner.setAdapter(playerListadapter);

        final Spinner chancSpinner = setupCard.findViewById(R.id.spinner_chancellor);
        chancSpinner.setAdapter(playerListadapter);
        chancSpinner.setSelection(1); //Setting a different item on the chancellor spinner so they don't have the same name at the beginning

        final Spinner presClaimSpinner = setupCard.findViewById(R.id.spinner_pres_claim);
        final ArrayAdapter<String> presClaimListadapter = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, Claim.getPresidentClaims());
        presClaimListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        presClaimSpinner.setAdapter(presClaimListadapter);

        final Spinner chancClaimSpinner = setupCard.findViewById(R.id.spinner_chanc_claim);
        ArrayAdapter<String> chancClaimListadapter = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, Claim.getChancellorClaims());
        chancClaimListadapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        chancClaimSpinner.setAdapter(chancClaimListadapter);


        //Initialise all other important bits
        final LinearLayout ll_policyplayed = setupCard.findViewById(R.id.ll_policy_outcome);
        final CheckBox cb_vetoed = setupCard.findViewById(R.id.checkBox_policy_vetoed);
        final Switch sw_votingoutcome = setupCard.findViewById(R.id.switch_vote_outcome);
        final ImageView iv_fascist = setupCard.findViewById(R.id.img_policy_fascist);
        final ImageView iv_liberal = setupCard.findViewById(R.id.img_policy_liberal);
        final FloatingActionButton fab_create = setupCard.findViewById(R.id.fab_create);
        ImageView iv_cancel = setupCard.findViewById(R.id.img_cancel);

        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.removeAllViews();
            }
        });

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

        iv_liberal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_liberal.setAlpha((float) 1);
                iv_fascist.setAlpha((float) 0.2);

                ColorStateList csl = ColorStateList.valueOf(c.getColor(R.color.colorLiberal));
                fab_create.setBackgroundTintList(csl);
                cb_vetoed.setButtonTintList(csl);
            }
        });

        iv_fascist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_fascist.setAlpha((float) 1);
                iv_liberal.setAlpha((float) 0.2);

                ColorStateList csl = ColorStateList.valueOf(c.getColor(R.color.colorFascist));
                fab_create.setBackgroundTintList(csl);
                cb_vetoed.setButtonTintList(csl);
            }
        });

        fab_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //We now have to check a few things before creating the event. For this, we create the boolean pass. If it is false at the end of the check, we won't create the event
                boolean pass = true;

                //Firstly, the name of president and chancellor cannot be the same
                if(presSpinner.getSelectedItem().equals(chancSpinner.getSelectedItem())) {
                    pass = false;
                    Toast.makeText(c, c.getString(R.string.err_names_cannot_be_the_same), Toast.LENGTH_LONG).show();
                }

                //Maybe add a check for claims in the future? E.g. RRR and BB should create an error
                if(pass) {
                    boolean voteRejected = sw_votingoutcome.isChecked();
                    String presName = (String) presSpinner.getSelectedItem();
                    String chancName = (String) chancSpinner.getSelectedItem();

                    VoteEvent voteEvent = new VoteEvent(presName, chancName, voteRejected ? VoteEvent.VOTE_FAILED : VoteEvent.VOTE_PASSED, c);
                    ClaimEvent claimEvent;

                    if(voteRejected) claimEvent = null;
                    else {
                        int presClaim = Claim.getClaimInt((String) presClaimSpinner.getSelectedItem());
                        int chancClaim = Claim.getClaimInt((String) chancClaimSpinner.getSelectedItem());

                        int playedPolicy = (iv_fascist.getAlpha() == (float) 1) ? Claim.FASCIST : Claim.LIBERAL;
                        boolean vetoed = cb_vetoed.isChecked();

                        claimEvent = new ClaimEvent(presName, chancName, presClaim, chancClaim, playedPolicy, vetoed, c);
                    }

                    LegislativeSession legislativeSession = new LegislativeSession(voteEvent, claimEvent, c);
                    GameLog.addEvent(legislativeSession);
                    linearLayout.removeAllViews();
                }
            }
        });

        linearLayout.addView(setupCard);
    }

}

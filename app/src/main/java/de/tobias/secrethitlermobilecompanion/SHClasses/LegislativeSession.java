package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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

    public LegislativeSession(VoteEvent voteEvent, ClaimEvent claimEvent, Context context) {
        sessionNumber = GameLog.legSessionNo++;
        this.voteEvent = voteEvent;
        this.claimEvent = claimEvent;
        c = context;
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

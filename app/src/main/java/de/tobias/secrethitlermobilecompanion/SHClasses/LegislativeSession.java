package de.tobias.secrethitlermobilecompanion.SHClasses;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import de.tobias.secrethitlermobilecompanion.R;

public class LegislativeSession extends GameEvent {

    private int sessionNumber;
    private VoteEvent voteEvent;
    private ClaimEvent claimEvent;
    private Context c;

    public LegislativeSession(VoteEvent voteEvent, ClaimEvent claimEvent, Context context) {
        sessionNumber = GameLog.legSessionNo++;
        this.voteEvent = voteEvent;
        this.claimEvent = claimEvent;
        c = context;
    }

    public void setupCard(CardView cardLayout) {
        TextView title = cardLayout.findViewById(R.id.title);
        TextView presName = cardLayout.findViewById(R.id.pres_name);
        TextView chancName = cardLayout.findViewById(R.id.chanc_name);

        TextView presClaim = cardLayout.findViewById(R.id.pres_claim);
        TextView chancClaim = cardLayout.findViewById(R.id.chanc_claim);

        TextView playedPolicytv = cardLayout.findViewById(R.id.policy_played);
        ImageView playedPolicyLogo = cardLayout.findViewById(R.id.img_policy_played);


        presName.setText(voteEvent.getPresidentName());
        chancName.setText(voteEvent.getChancellorName());

        if(voteEvent.getVotingResult() == VoteEvent.VOTE_FAILED) {
            title.setText(c.getString(R.string.legislative_session)+ " #"+sessionNumber + c.getString(R.string.rejected));

            //Hide unnecessary stuff
            chancClaim.setVisibility(View.GONE);
            presClaim.setVisibility(View.GONE);
            playedPolicytv.setVisibility(View.GONE);
            playedPolicyLogo.setVisibility(View.GONE);

            chancName.setTextColor(Color.RED);
            chancName.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            title.setText(c.getString(R.string.legislative_session)+ " #"+sessionNumber);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                chancClaim.setText(Html.fromHtml(Claim.getClaimString(c, claimEvent.getChancellorClaim()),  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
                presClaim.setText(Html.fromHtml(Claim.getClaimString(c, claimEvent.getPresidentClaim()),  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
            } else {
                chancClaim.setText(Html.fromHtml(Claim.getClaimString(c, claimEvent.getChancellorClaim())), TextView.BufferType.SPANNABLE);
                presClaim.setText(Html.fromHtml(Claim.getClaimString(c, claimEvent.getPresidentClaim())), TextView.BufferType.SPANNABLE);
            }

            if(claimEvent.getPlayedPolicy() == Claim.LIBERAL) playedPolicyLogo.setImageDrawable(c.getDrawable(R.drawable.liberal_logo));
        }

        if(claimEvent != null && claimEvent.isVetoed()) {
            title.setText(c.getString(R.string.legislative_session) + " #" + sessionNumber + c.getString(R.string.vetoed));
            playedPolicytv.setTextColor(Color.RED);
            playedPolicytv.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

}

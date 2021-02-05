package de.tobiundmario.secrethitlermobilecompanion.SHEvents;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager;

public class TopPolicyPlayedEvent extends GameEvent {

    private int policyPlayed = -1;
    private Context c;

    private LegislativeSession linkedLegislativeSession;

    public TopPolicyPlayedEvent(int policyPlayed, Context c) {
        this.c = c;
        this.policyPlayed = policyPlayed;
        isSetup = false;
        triggerPolicyChange(policyPlayed, false);
    }

    public TopPolicyPlayedEvent(Context c) {
        this.c = c;
        isSetup = true;
    }

    public int getPolicyPlayed() {
        return policyPlayed;
    }

    public LegislativeSession getLinkedLegislativeSession() {
        return linkedLegislativeSession;
    }

    public void setLinkedLegislativeSession(LegislativeSession linkedLegislativeSession) {
        this.linkedLegislativeSession = linkedLegislativeSession;
    }

    private void playSound() {
        if(GameEventsManager.policySounds) {
            MediaPlayer mp;
            if (policyPlayed == Claim.LIBERAL) mp = MediaPlayer.create(c, R.raw.enactpolicyl);
            else mp = MediaPlayer.create(c, R.raw.enactpolicyf);
            mp.start();
        }
    }

    public void triggerPolicyChange(int policyPlayed, boolean changed) {
        if(policyPlayed == Claim.LIBERAL) {
            GameManager.liberalPolicies++;
            if(changed) GameManager.fascistPolicies--;
        } else {
            GameManager.fascistPolicies++;
            if(changed) GameManager.liberalPolicies--;
        }
    }

    private void processChanges(int oldPolicyPlayed, int policyPlayed) {
        if(oldPolicyPlayed == policyPlayed) return;
        triggerPolicyChange(policyPlayed, oldPolicyPlayed != -1);
    }

    public void undoChanges() {
        if(policyPlayed == Claim.LIBERAL) {
            GameManager.liberalPolicies--;
        } else {
            GameManager.fascistPolicies--;
        }
    }

    @Override
    public void initialiseSetupCard(CardView cardView) {
        final ImageView iv_fascist = cardView.findViewById(R.id.img_fascist_policy);
        final ImageView iv_liberal = cardView.findViewById(R.id.img_liberal_policy);
        final Button btn_create = cardView.findViewById(R.id.btn_setup_forward);

        iv_fascist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_liberal.setAlpha(0.2f);
                iv_fascist.setAlpha(1f);
            }
        });

        iv_liberal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_liberal.setAlpha(1f);
                iv_fascist.setAlpha(0.2f);
            }
        });

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newPolicyPlayed = (iv_fascist.getAlpha() == 1f) ? Claim.FASCIST : Claim.LIBERAL;
                processChanges(policyPlayed, newPolicyPlayed);
                policyPlayed = newPolicyPlayed;
                isSetup = false;
                GameEventsManager.notifySetupPhaseLeft(TopPolicyPlayedEvent.this);
                playSound();
            }
        });
    }

    @Override
    public void setCurrentValues(CardView cardView) {
        final ImageView iv_fascist = cardView.findViewById(R.id.img_fascist_policy);
        final ImageView iv_liberal = cardView.findViewById(R.id.img_liberal_policy);

        if(policyPlayed == Claim.LIBERAL) {
            iv_liberal.setAlpha(1f);
            iv_fascist.setAlpha(0.2f);
        } else {
            iv_liberal.setAlpha(0.2f);
            iv_fascist.setAlpha(1f);
        }
    }

    @Override
    public void initialiseCard(CardView cardView) {
        final ImageView iv_policy = cardView.findViewById(R.id.img_policy);
        Drawable policyDrawable = (policyPlayed == Claim.LIBERAL) ? (ContextCompat.getDrawable(c, R.drawable.liberal_logo)) : (ContextCompat.getDrawable(c, R.drawable.fascist_logo));
        iv_policy.setImageDrawable(policyDrawable);
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(List<String> unselectedPlayers) {
        return false;
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("id", id);
        obj.put("type", "top_policy");
        obj.put("policy_played", Claim.getClaimStringForJSON(c, policyPlayed));

        return obj;
    }
}

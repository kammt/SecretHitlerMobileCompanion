package de.tobiundmario.secrethitlermobilecompanion.SHCards;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Switch;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.MainActivity;
import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PlayerList;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;

public class GameSetupCard extends GameEvent {

    Context c;

    public GameSetupCard(Context context) {
        isSetup = true;
        this.c = context;

        permitEditing = false;
    }

    @Override
    public void setupSetupCard(final CardView cardView) {
        final Switch sw_sounds_execution = cardView.findViewById(R.id.switch_sounds_execution);
        final Switch sw_sounds_policy = cardView.findViewById(R.id.switch_sounds_policy);
        final Switch sw_server = cardView.findViewById(R.id.switch_server);
        final Switch sw_sounds_end = cardView.findViewById(R.id.switch_sounds_end);

        FloatingActionButton fab_create = cardView.findViewById(R.id.fab_create);
        fab_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlphaAnimation fadeoutAnimation = new AlphaAnimation((float) 1, (float) 0);
                fadeoutAnimation.setDuration(500);
                fadeoutAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        GameLog.remove(GameSetupCard.this);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                final MainActivity mainActivity = ((MainActivity) c);
                int playerCount = PlayerList.getPlayerList().size();
                if(playerCount == 0) {
                    new AlertDialog.Builder(c)
                            .setTitle(c.getString(R.string.no_players_added))
                            .setMessage(c.getString(R.string.no_players_added_msg))
                            .setPositiveButton(c.getString(R.string.btn_ok), null)
                            .show();
                } else if (playerCount == 1) {
                    new AlertDialog.Builder(c)
                            .setTitle(c.getString(R.string.title_too_little_players))
                            .setMessage(c.getString(R.string.no_players_added_msg))
                            .setPositiveButton(c.getString(R.string.btn_ok), null)
                            .show();
                } else if (playerCount < 5 && playerCount >= 2) {
                    new AlertDialog.Builder(c)
                            .setMessage(c.getString(R.string.msg_too_little_players, playerCount))
                            .setTitle(c.getString(R.string.title_too_little_players))
                            .setNegativeButton(c.getString(R.string.dialog_mismatching_claims_btn_cancel), null)
                            .setPositiveButton(c.getString(R.string.dialog_mismatching_claims_btn_continue), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mainActivity.startGame(sw_sounds_execution.isChecked(), sw_sounds_policy.isChecked(), sw_sounds_end.isChecked(), sw_server.isChecked());
                                    cardView.startAnimation(fadeoutAnimation);
                                }
                            })
                            .show();
                } else {
                    mainActivity.startGame(sw_sounds_execution.isChecked(), sw_sounds_policy.isChecked(), sw_sounds_end.isChecked(), sw_server.isChecked());
                    cardView.startAnimation(fadeoutAnimation);
                }
            }
        });


        AlphaAnimation alphaAnimation = new AlphaAnimation((float) 0, (float) 1);
        alphaAnimation.setDuration(500);
        cardView.startAnimation(alphaAnimation);
    }

    @Override
    public void setCurrentValues(CardView cardView) {
        //This will never be called
    }

    @Override
    public void setupCard(CardView cardView) {
        //This method will never be called, we couldn't care less
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return false; //This method will never be called, we couldn't care less
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        return null; //This method will never be called, we couldn't care less
    }
}

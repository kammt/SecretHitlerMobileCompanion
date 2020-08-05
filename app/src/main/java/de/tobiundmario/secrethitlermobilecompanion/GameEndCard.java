package de.tobiundmario.secrethitlermobilecompanion;

import android.content.Context;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;

public class GameEndCard extends GameEvent {

    private Context context;
    private MainActivity mainActivity;

    public GameEndCard(Context context) {
        isSetup = true;
        this.context = context;
        mainActivity = (MainActivity) context; //To access the setGameMode function later on

        permitEditing = false;
    }

    @Override
    public void setupSetupCard(CardView cardView) {
        final ImageView iv_fascist = cardView.findViewById(R.id.img_policy_fascist);
        final ImageView iv_liberal = cardView.findViewById(R.id.img_policy_liberal);
        final FloatingActionButton fab_end = cardView.findViewById(R.id.fab_create);

        final RadioButton rb_hitler = cardView.findViewById(R.id.rb_win_option_hitler);
        final RadioButton rb_policy = cardView.findViewById(R.id.rb_win_option_policy);

        if(GameLog.getLiberalPolicies() == 4) {
            iv_liberal.setAlpha((float) 1);
            iv_fascist.setAlpha((float) 0.2);

            ColorStateList csl = ColorStateList.valueOf(context.getColor(R.color.colorLiberal));
            fab_end.setBackgroundTintList(csl);
            rb_hitler.setButtonTintList(csl);
            rb_policy.setButtonTintList(csl);

            rb_hitler.setText(context.getString(R.string.liberals_won_hitler));
            rb_policy.setText(context.getString(R.string.liberals_won_policies));
        }

        iv_liberal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_liberal.setAlpha((float) 1);
                iv_fascist.setAlpha((float) 0.2);

                ColorStateList csl = ColorStateList.valueOf(context.getColor(R.color.colorLiberal));
                fab_end.setBackgroundTintList(csl);
                rb_hitler.setButtonTintList(csl);
                rb_policy.setButtonTintList(csl);

                rb_hitler.setText(context.getString(R.string.liberals_won_hitler));
                rb_policy.setText(context.getString(R.string.liberals_won_policies));
            }
        });

        iv_fascist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_fascist.setAlpha((float) 1);
                iv_liberal.setAlpha((float) 0.2);

                ColorStateList csl = ColorStateList.valueOf(context.getColor(R.color.colorFascist));
                fab_end.setBackgroundTintList(csl);
                rb_hitler.setButtonTintList(csl);
                rb_policy.setButtonTintList(csl);

                rb_hitler.setText(context.getString(R.string.fascists_won_hitler));
                rb_policy.setText(context.getString(R.string.fascists_win_policies));
            }
        });

        fab_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean fascistsWon = (iv_fascist.getAlpha() == (float) 1);
                if(fascistsWon && rb_hitler.isChecked()) {
                    //Fascists won, hitler elected
                    if(GameLog.endSounds) MediaPlayer.create(context, R.raw.fascistswinhitlerelected).start();
                } else if(fascistsWon && rb_policy.isChecked()) {
                    //Fascists won, 5 policies
                    if(GameLog.endSounds) MediaPlayer.create(context, R.raw.fascistswin).start();
                } else if (!fascistsWon && rb_hitler.isChecked()) {
                    //Liberals won, hitler shot
                    if(GameLog.endSounds) MediaPlayer.create(context, R.raw.liberalswin).start();
                } else {
                    //Liberals won, 4 policies
                    if(GameLog.endSounds) MediaPlayer.create(context, R.raw.liberalswin).start();
                }
                mainActivity.endGame();
            }
        });
    }

    @Override
    public void setCurrentValues(CardView cardView) {
        //This will never be called
    }

    @Override
    public void setupCard(CardView cardView) {
        //This will never be called
    }

    @Override
    public boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers) {
        return false; //This will never be called
    }

    @Override
    public JSONObject getJSON() throws JSONException {
        return null; //This will never be called
    }
}

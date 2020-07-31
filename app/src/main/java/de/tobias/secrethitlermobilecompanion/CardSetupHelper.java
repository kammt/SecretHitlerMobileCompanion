package de.tobias.secrethitlermobilecompanion;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import de.tobias.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobias.secrethitlermobilecompanion.SHClasses.PlayerList;

public class CardSetupHelper {
    /*
    This class is responsible for displaying the setup for each card. It is called from the onClickListeners in the MainActivity
     */
    public static final int GAME_SETUP = 200;


    public static void setupCard(final LinearLayout linearLayout, final int cardType, final Context context) {
        /*
        This function will check if there is another "Setup-Card" present and will ask the user to either replace the old one or cancel the operation.
        This helps to keep the screen cleaner, as the LinearLayout is not scrollable and thus does not support multiple setup-Cards
         */
        if(linearLayout.getChildCount() > 0) {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.dialog_another_setup_title))
                    .setMessage(context.getString(R.string.dialog_another_setup_desc))
                    .setPositiveButton(context.getString(R.string.dialog_another_setup_btn_replace), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            linearLayout.removeAllViews();
                            callSetupFunction(linearLayout, cardType, context);
                        }
                    })
                    .setNegativeButton(context.getString(R.string.dialog_another_setup_btn_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Just do nothing
                        }
                    })
                    .show();
        } else callSetupFunction(linearLayout, cardType, context); //The child count is 0 => Layout is empty
    }

    private static void callSetupFunction(LinearLayout linearLayout, int cardType, Context context) {
        /*
        This function is to save code, as this will be executed in two scenarios:
            - layout is empty
            - "Replace" button is clicked
         */

        switch(cardType) {
            case GAME_SETUP:
                setupGame(linearLayout, context);
                break;
            default:
                throw new IllegalArgumentException("Unknown cardType specified!");
        }
    }

    private static void setupGame(final LinearLayout linearLayout, final Context c) {
        final CardView setupCard = (CardView) LayoutInflater.from(c).inflate(R.layout.setup_card_game_settings, linearLayout, false);

        final Switch sw_sounds_execution = setupCard.findViewById(R.id.switch_sounds_execution);
        final Switch sw_sounds_policy = setupCard.findViewById(R.id.switch_sounds_policy);
        final Switch sw_server = setupCard.findViewById(R.id.switch_server);

        FloatingActionButton fab_create = setupCard.findViewById(R.id.fab_create);
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
                        linearLayout.removeAllViews();
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
                                    mainActivity.startGame(sw_sounds_execution.isChecked(), sw_sounds_policy.isChecked(), sw_server.isChecked());
                                    setupCard.startAnimation(fadeoutAnimation);
                                }
                            })
                            .show();
                } else {
                    mainActivity.startGame(sw_sounds_execution.isChecked(), sw_sounds_policy.isChecked(), sw_server.isChecked());
                    setupCard.startAnimation(fadeoutAnimation);

                }
            }
        });

        linearLayout.addView(setupCard);


        AlphaAnimation alphaAnimation = new AlphaAnimation((float) 0, (float) 1);
        alphaAnimation.setDuration(500);
        setupCard.startAnimation(alphaAnimation);
    }


    public static ArrayAdapter<String> getPlayerNameAdapter(final Context context) {
        return new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, PlayerList.getAlivePlayerList()) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                ((TextView) v).setTypeface(externalFont);

                return v;
            }


            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                ((TextView) v).setTypeface(externalFont);

                return v;
            }
        };
    }

    public static ArrayAdapter<String> getClaimAdapter(final Context context, ArrayList<String> data) {
        return new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, data) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                TextView tv = (TextView) v;
                tv.setTypeface(externalFont);
                tv.setText(Claim.colorClaim(tv.getText().toString()), TextView.BufferType.SPANNABLE);

                return v;
            }


            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                TextView tv = (TextView) v;
                tv.setTypeface(externalFont);
                tv.setText(Claim.colorClaim(tv.getText().toString()), TextView.BufferType.SPANNABLE);

                return v;
            }
        };
    }
}

package de.tobiundmario.secrethitlermobilecompanion;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrackSelectionManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PlayerList;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.SharedPreferencesManager;

public class SetupFragment extends Fragment {

    RecyclerView playerCardList;

    private Button btn_setup_forward;
    private Button btn_setup_back;
    private ConstraintLayout container_setup_buttons;

    private ConstraintLayout container_new_player;

    private ConstraintLayout container_select_track;
    public TextView tv_title_custom_tracks;

    private ConstraintLayout container_settings;
    public TextView tv_choose_from_previous_games_players;

    private ProgressBar progressBar_setupSteps;

    private View.OnClickListener listener_backward_players;
    private View.OnClickListener listener_forward_players;

    private View.OnClickListener listener_backward_tracks;
    private View.OnClickListener listener_forward_tracks;

    private View.OnClickListener listener_backward_settings;
    private View.OnClickListener listener_forward_settings;

    private Context context;

    public SetupFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup, container, false);
    }

    public void start() {
        initialiseLayout(getView());
        startSetup();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
    }

    public void startSetup() {
        //Resetting values in case there has been a setup before which was cancelled
        PlayerList.initialise(playerCardList, context);

        container_setup_buttons.setVisibility(View.VISIBLE);
        container_new_player.setVisibility(View.VISIBLE);
        progressBar_setupSteps.setVisibility(View.VISIBLE);

        Animation progressBarAnimation = new MainActivity.ProgressBarAnimation(progressBar_setupSteps, 0, 300);
        progressBarAnimation.setDuration(500);
        progressBar_setupSteps.startAnimation(progressBarAnimation);
    }

    public void previousPage() {
        btn_setup_back.callOnClick();
    }

    public void initialiseLayout(final View fragmentLayout) {
        btn_setup_back = fragmentLayout.findViewById(R.id.btn_setup_back);
        btn_setup_forward = fragmentLayout.findViewById(R.id.btn_setup_forward);
        container_setup_buttons = fragmentLayout.findViewById(R.id.setup_buttons);

        container_new_player = fragmentLayout.findViewById(R.id.container_setup_add_players);
        tv_choose_from_previous_games_players = fragmentLayout.findViewById(R.id.tv_choose_old_players);

        container_select_track = fragmentLayout.findViewById(R.id.container_setup_set_Track);
        tv_title_custom_tracks = fragmentLayout.findViewById(R.id.tv_title_custom_tracks);

        container_settings = fragmentLayout.findViewById(R.id.container_setup_settings);

        playerCardList = fragmentLayout.findViewById(R.id.playerList);
        playerCardList.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        PlayerList.initialise(playerCardList, context);

        progressBar_setupSteps = fragmentLayout.findViewById(R.id.progressBar_setupProgress);
        progressBar_setupSteps.setMax(900);

        RecyclerView pastPlayerLists = fragmentLayout.findViewById(R.id.oldPlayerLists);
        SharedPreferencesManager.setupOldPlayerListRecyclerView(pastPlayerLists, context);

        RecyclerView fascistTracks = fragmentLayout.findViewById(R.id.list_custom_tracks);
        SharedPreferencesManager.setupCustomTracksRecyclerView(fascistTracks, context);

        final FloatingActionButton fab_newTrack = fragmentLayout.findViewById(R.id.fab_create_custom_track);
        fab_newTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardDialog.showTrackCreationDialog(context);
            }
        });

        FascistTrackSelectionManager.setupOfficialTrackList((LinearLayout) fragmentLayout.findViewById(R.id.container_official_tracks), context);

        listener_backward_players = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation progressBarAnimation = new MainActivity.ProgressBarAnimation(progressBar_setupSteps, 250, 0);
                progressBarAnimation.setDuration(500);
                progressBar_setupSteps.startAnimation(progressBarAnimation);

                ((MainActivity) context).replaceFragment(MainActivity.main, true);
            }
        };

        listener_forward_players = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable continueSetup = new Runnable() {
                    @Override
                    public void run() {
                        //Check if there is a recommended track available
                        LinearLayout container_recommended_track = fragmentLayout.findViewById(R.id.container_recommended_track);

                        if(PlayerList.getPlayerList().size() >=5 && PlayerList.getPlayerList().size() <=10) {//Recommended track available
                            container_recommended_track.setVisibility(View.VISIBLE);

                            int recommendation = -10;
                            if(PlayerList.getPlayerList().size() == 5 || PlayerList.getPlayerList().size() == 6) {
                                recommendation = 0;
                            } else if(PlayerList.getPlayerList().size() == 7 || PlayerList.getPlayerList().size() == 8) {
                                recommendation = 1;
                            } else if(PlayerList.getPlayerList().size() == 9 || PlayerList.getPlayerList().size() ==10) {
                                recommendation = 2;
                            }

                            //Only change the layout if the recommendation changed
                            if(recommendation != FascistTrackSelectionManager.recommendedTrackIndex) {
                                CardView cv_recommended_track = (CardView) getLayoutInflater().inflate(R.layout.card_official_track, container_recommended_track, false);

                                if (recommendation == 0) {
                                    FascistTrackSelectionManager.setupOfficialCard(cv_recommended_track, FascistTrackSelectionManager.TRACK_TYPE_5_TO_6, context);
                                } else if (recommendation == 1) {
                                    FascistTrackSelectionManager.setupOfficialCard(cv_recommended_track, FascistTrackSelectionManager.TRACK_TYPE_7_TO_8, context);
                                } else if (recommendation == 2) {
                                    FascistTrackSelectionManager.setupOfficialCard(cv_recommended_track, FascistTrackSelectionManager.TRACK_TYPE_9_TO_10, context);
                                }

                                if (container_recommended_track.getChildCount() > 1)
                                    container_recommended_track.removeViewAt(1); //If there is already a recommended track in there, we remove it.

                                container_recommended_track.addView(cv_recommended_track);
                                FascistTrackSelectionManager.changeRecommendedCard(recommendation, cv_recommended_track, context);
                            }
                        } else {
                            container_recommended_track.setVisibility(View.GONE);
                            FascistTrackSelectionManager.recommendedTrackIndex = -1;
                            FascistTrackSelectionManager.recommendedCard = null;
                        }

                        //Change the back listener
                        btn_setup_back.setOnClickListener(listener_backward_tracks);
                        btn_setup_forward.setOnClickListener(listener_forward_tracks);

                        //Animations
                        Animation slideOutLeft = AnimationUtils.loadAnimation(context, R.anim.slide_out_left);
                        container_new_player.startAnimation(slideOutLeft);

                        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                container_new_player.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });

                        fab_newTrack.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_open));

                        Animation progressBarAnimation = new MainActivity.ProgressBarAnimation(progressBar_setupSteps, 300, 600);
                        progressBarAnimation.setDuration(500);
                        progressBar_setupSteps.startAnimation(progressBarAnimation);

                        Animation slideInRight = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
                        container_select_track.setVisibility(View.VISIBLE);
                        container_select_track.startAnimation(slideInRight);
                    }
                };

                int playerCount = PlayerList.getPlayerList().size();
                if(playerCount <= 2) {
                    String title = (playerCount == 0) ? getString(R.string.no_players_added) : getString(R.string.title_too_little_players);
                    CardDialog.showMessageDialog(context, title, getString(R.string.no_players_added_msg), getString(R.string.btn_ok), null, null, null);
                } else if (playerCount < 5) {
                    CardDialog.showMessageDialog(context, getString(R.string.title_too_little_players), getString(R.string.msg_too_little_players, playerCount), getString(R.string.dialog_mismatching_claims_btn_continue), continueSetup, getString(R.string.dialog_mismatching_claims_btn_cancel), null);
                } else continueSetup.run();
            }
        };

        listener_backward_tracks = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation slideOutRight = AnimationUtils.loadAnimation(context, R.anim.slide_out_right);
                container_select_track.startAnimation(slideOutRight);

                slideOutRight.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        container_select_track.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                Animation slideInLeft = AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
                container_new_player.setVisibility(View.VISIBLE);
                container_new_player.startAnimation(slideInLeft);

                fab_newTrack.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_close));

                Animation progressBarAnimation = new MainActivity.ProgressBarAnimation(progressBar_setupSteps, 600, 300);
                progressBarAnimation.setDuration(500);
                progressBar_setupSteps.startAnimation(progressBarAnimation);
                btn_setup_forward.setOnClickListener(listener_forward_players);
                btn_setup_back.setOnClickListener(listener_backward_players);
            }
        };

        listener_forward_tracks = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable continueSetup = new Runnable() {
                    @Override
                    public void run() {
                        Animation slideOutLeft = AnimationUtils.loadAnimation(context, R.anim.slide_out_left);
                        container_select_track.startAnimation(slideOutLeft);

                        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                container_select_track.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });

                        Animation slideInRight = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
                        container_settings.setVisibility(View.VISIBLE);
                        container_settings.startAnimation(slideInRight);

                        fab_newTrack.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_close));

                        Animation progressBarAnimation = new MainActivity.ProgressBarAnimation(progressBar_setupSteps, 600, 900);
                        progressBarAnimation.setDuration(500);
                        progressBar_setupSteps.startAnimation(progressBarAnimation);

                        btn_setup_forward.setText(getString(R.string.start_game));
                        btn_setup_forward.setOnClickListener(listener_forward_settings);
                        btn_setup_back.setOnClickListener(listener_backward_settings);
                    }
                };

                if(GameLog.gameTrack == null) CardDialog.showMessageDialog(context, getString(R.string.no_track_selected), getString(R.string.no_track_selected_message), getString(R.string.btn_ok), null, null, null);
                else continueSetup.run();
            }
        };

        listener_backward_settings = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation slideOutRight = AnimationUtils.loadAnimation(context, R.anim.slide_out_right);
                container_settings.startAnimation(slideOutRight);

                slideOutRight.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        container_settings.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                Animation slideInLeft = AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
                container_select_track.setVisibility(View.VISIBLE);
                container_select_track.startAnimation(slideInLeft);

                fab_newTrack.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_open));

                Animation progressBarAnimation = new MainActivity.ProgressBarAnimation(progressBar_setupSteps, 900, 600);
                progressBarAnimation.setDuration(500);
                progressBar_setupSteps.startAnimation(progressBarAnimation);

                btn_setup_forward.setText(getString(R.string.dialog_mismatching_claims_btn_continue));
                btn_setup_forward.setOnClickListener(listener_forward_tracks);
                btn_setup_back.setOnClickListener(listener_backward_tracks);
            }
        };

        listener_forward_settings = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch sw_sounds_execution = fragmentLayout.findViewById(R.id.switch_execution);
                Switch sw_sonds_policy = fragmentLayout.findViewById(R.id.switch_policies);
                Switch sw_sounds_end = fragmentLayout.findViewById(R.id.switch_gameEnd);

                Switch sw_server = fragmentLayout.findViewById(R.id.switch_server);

                GameLog.endSounds = sw_sounds_end.isChecked();
                GameLog.policySounds = sw_sonds_policy.isChecked();
                GameLog.executionSounds = sw_sounds_execution.isChecked();

                GameLog.server = sw_server.isChecked();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Resetting view visibility from
                        container_new_player.setVisibility(View.VISIBLE);
                        container_settings.setVisibility(View.GONE);
                    }
                }, 1000);

                ((MainActivity) context).replaceFragment(MainActivity.game, true);
            }
        };

        btn_setup_forward.setOnClickListener(listener_forward_players);
        btn_setup_back.setOnClickListener(listener_backward_players);
    }
}
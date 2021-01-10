package de.tobiundmario.secrethitlermobilecompanion;

import android.content.Context;
import android.os.Bundle;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrackSelectionManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.SharedPreferencesManager;

public class SetupFragment extends Fragment {

    private RecyclerView playerCardList;

    private Button btn_setup_forward;
    private Button btn_setup_back;
    private ConstraintLayout container_setup_buttons;

    private ConstraintLayout container_new_player;

    private ConstraintLayout container_select_track;
    private FloatingActionButton fab_newTrack;
    public TextView tv_title_custom_tracks;

    private ConstraintLayout container_settings;
    public TextView tv_choose_from_previous_games_players;

    private ProgressBar progressBar_setupSteps;

    private Context context;

    private int page = 1;
    private ConstraintLayout[] pages;
    private int progressBar_value = 300;
    private final int progressBar_steps = 300;
    private int progressBar_newValue = 0;
    private SetupContinueCondition[] setupContinueConditions;

    public SetupFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
    }

    public void startSetup() {
        //Resetting values in case there has been a setup before which was cancelled
        PlayerListManager.initialise(playerCardList, context);
        FascistTrackSelectionManager.selectedTrackIndex = -1;
        FascistTrackSelectionManager.recommendedTrackIndex = -1;
        FascistTrackSelectionManager.previousSelection = null;
        FascistTrackSelectionManager.initialise();
        FascistTrackSelectionManager.setupOfficialTrackList((LinearLayout) getView().findViewById(R.id.container_official_tracks), context);

        //Initialising RecyclerViews
        RecyclerView pastPlayerLists = getView().findViewById(R.id.oldPlayerLists);
        SharedPreferencesManager.setupOldPlayerListRecyclerView(pastPlayerLists, context);

        RecyclerView fascistTracks = getView().findViewById(R.id.list_custom_tracks);
        SharedPreferencesManager.setupCustomTracksRecyclerView(fascistTracks, context);

        //Resetting view visibility
        container_settings.setVisibility(View.GONE);

        //Resetting button
        btn_setup_forward.setText(context.getString(R.string.btn_continue));

        container_setup_buttons.setVisibility(View.VISIBLE);
        container_new_player.setVisibility(View.VISIBLE);
        progressBar_setupSteps.setVisibility(View.VISIBLE);

        Animation progressBarAnimation = new MainActivity.ProgressBarAnimation(progressBar_setupSteps, 0, 300);
        progressBarAnimation.setDuration(500);
        progressBar_setupSteps.startAnimation(progressBarAnimation);
    }

    public void initialiseLayout() {
        final View fragmentLayout = getView();

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
        PlayerListManager.initialise(playerCardList, context);

        progressBar_setupSteps = fragmentLayout.findViewById(R.id.progressBar_setupProgress);
        progressBar_setupSteps.setMax(900);

        setupContinueConditions = new SetupContinueCondition[] {firstCondition(), secondCondition()};
        pages = new ConstraintLayout[] {container_new_player, container_select_track, container_settings};

        fab_newTrack = fragmentLayout.findViewById(R.id.fab_create_custom_track);
        fab_newTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardDialog.showTrackCreationDialog(context);
            }
        });

        btn_setup_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSetupPage(false);
            }
        });
        btn_setup_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousSetupPage();
            }
        });
    }

    private SetupContinueCondition firstCondition() {
        //When switching from page 1 to 2
        return new SetupContinueCondition() {
            @Override
            public boolean shouldSetupContinue() {
                int playerCount = PlayerListManager.getPlayerList().size();
                return playerCount >= 5;
            }

            @Override
            public void showErrorMessage() {
                int playerCount = PlayerListManager.getPlayerList().size();
                if(playerCount <= 2) {
                    String title = (playerCount == 0) ? getString(R.string.no_players_added) : getString(R.string.title_too_little_players);
                    CardDialog.showMessageDialog(context, title, getString(R.string.no_players_added_msg), getString(R.string.btn_ok), null, null, null);
                } else if (playerCount < 5) {
                    CardDialog.showMessageDialog(context, getString(R.string.title_too_little_players), getString(R.string.msg_too_little_players, playerCount), getString(R.string.btn_continue), new Runnable() {
                        @Override
                        public void run() {
                            nextSetupPage(true);
                        }
                    }, getString(R.string.btn_cancel), null);
                }
            }

            @Override
            public void initialiseLayout() {
                //Check if there is a recommended track available
                LinearLayout container_recommended_track = getView().findViewById(R.id.container_recommended_track);

                if(PlayerListManager.getPlayerList().size() >=5 && PlayerListManager.getPlayerList().size() <=10) {//Recommended track available
                    container_recommended_track.setVisibility(View.VISIBLE);
                    FascistTrackSelectionManager.setupRecommendedCard(getLayoutInflater(), container_recommended_track);
                } else {
                    container_recommended_track.setVisibility(View.GONE);
                    FascistTrackSelectionManager.recommendedTrackIndex = -1;
                    FascistTrackSelectionManager.recommendedCard = null;
                }

                fab_newTrack.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_open));
            }
        };
    }

    private SetupContinueCondition secondCondition() {
        //When switching from page 2 to 3
        return new SetupContinueCondition() {
            @Override
            public boolean shouldSetupContinue() {
                return GameManager.gameTrack != null;
            }

            @Override
            public void showErrorMessage() {
                CardDialog.showMessageDialog(context, getString(R.string.no_track_selected), getString(R.string.no_track_selected_message), getString(R.string.btn_ok), null, null, null);
            }

            @Override
            public void initialiseLayout() {
                fab_newTrack.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_close));
            }
        };
    }

    public void nextSetupPage(boolean forceNextPage) {
        SetupContinueCondition condition = null;
        if(page != 3) condition = setupContinueConditions[page - 1];

        if(condition == null || condition.shouldSetupContinue() || forceNextPage) {
            page++;
            final ConstraintLayout oldPage, newPage;

            if(page <= 3) {
                oldPage = pages[page - 2];
                newPage = pages[page - 1];


                Animation slideInRight = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
                Animation slideOutLeft = AnimationUtils.loadAnimation(context, R.anim.slide_out_left);

                progressBar_newValue = progressBar_value + progressBar_steps;

                animateTransition(oldPage, newPage, slideOutLeft, slideInRight, false);

                assert condition != null;
                condition.initialiseLayout();

                if(page == 3) btn_setup_forward.setText(getString(R.string.start_game));
            } else finishSetup();
        } else  condition.showErrorMessage();
    }

    public void previousSetupPage() {
        page--;
        final ConstraintLayout oldPage, newPage;

        if(page >= 1) {
            oldPage = pages[page];
            newPage = pages[page - 1];

            Animation slideInLeft = AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
            Animation slideOutRight = AnimationUtils.loadAnimation(context, R.anim.slide_out_right);

            progressBar_newValue = progressBar_value - progressBar_steps;

            if(page == 1) fab_newTrack.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_close));
            animateTransition(oldPage, newPage, slideOutRight, slideInLeft, false);

            if(page == 2) btn_setup_forward.setText(getString(R.string.btn_continue));
        } else cancelSetup();
    }

    private void animateTransition(final ConstraintLayout oldPage, ConstraintLayout newPage, Animation slide_Out, Animation slide_in, boolean progressBarOnly) {
        if(!progressBarOnly) {
            newPage.setVisibility(View.VISIBLE);
            newPage.startAnimation(slide_in);

            slide_Out.setAnimationListener(new Animation.AnimationListener() {
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
            oldPage.startAnimation(slide_Out);
        }

        Animation progressBarAnimation = new MainActivity.ProgressBarAnimation(progressBar_setupSteps, progressBar_value, progressBar_newValue);
        progressBarAnimation.setDuration(500);
        progressBar_setupSteps.startAnimation(progressBarAnimation);
        progressBar_value = progressBar_newValue;
    }

    private void cancelSetup() {
        ((MainActivity) context).replaceFragment(MainActivity.page_main, true);
        progressBar_newValue = 0;
        animateTransition(null, null, null, null, true);
    }

    private void finishSetup() {
        View fragmentLayout = getView();
        GameEventsManager.endSounds = ((Switch) fragmentLayout.findViewById(R.id.switch_gameEnd)).isChecked();
        GameEventsManager.policySounds = ((Switch) fragmentLayout.findViewById(R.id.switch_policies)).isChecked();
        GameEventsManager.executionSounds = ((Switch) fragmentLayout.findViewById(R.id.switch_execution)).isChecked();

        GameEventsManager.server = ((Switch) fragmentLayout.findViewById(R.id.switch_server)).isChecked();

        //Delete Variables to save memory
        container_settings = null;
        container_select_track = null;
        container_new_player = null;
        container_setup_buttons = null;

        progressBar_setupSteps = null;
        btn_setup_back = null;
        btn_setup_forward = null;

        tv_choose_from_previous_games_players = null;
        tv_title_custom_tracks = null;
        playerCardList = null;

        setupContinueConditions = null;
        pages = null;
        fab_newTrack = null;

        ((MainActivity) context).replaceFragment(MainActivity.game, true);
    }

    interface SetupContinueCondition {
        abstract boolean shouldSetupContinue();
        abstract void showErrorMessage();
        abstract void initialiseLayout();
    }
}


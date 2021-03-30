package de.tobiundmario.secrethitlermobilecompanion;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.tobiundmario.secrethitlermobilecompanion.SHCards.FascistTrackCreationDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.FascistTrackSelectionManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.RecyclerViewManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.SetupFragmentManager;

public class SetupFragment extends Fragment {

    private RecyclerView playerCardList;

    private Button btn_setup_forward;
    private Button btn_setup_back;
    private ConstraintLayout container_setup_buttons;

    private ConstraintLayout setup_container_new_player;

    private ConstraintLayout setup_container_select_track;
    public FloatingActionButton fab_newTrack;
    public TextView tv_title_custom_tracks;
    public SwitchCompat switch_enable_tracks;
    public ConstraintLayout container_fascist_tracks;

    private ConstraintLayout setup_container_settings;
    public TextView tv_choose_from_previous_games_players;

    public ProgressBar progressBar_setupSteps;

    private Context context;

    private int page = 1;
    private ConstraintLayout[] pages;
    public int progressBar_value = 300;
    public final int progressBar_steps = 300;
    public int progressBar_newValue = 0;
    private SetupFragmentManager.SetupContinueCondition[] setupContinueConditions;

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
        resetValues();

        //Initialising RecyclerViews
        RecyclerView pastPlayerLists = getView().findViewById(R.id.oldPlayerLists);
        RecyclerViewManager.initialiseSetupRecyclerView(pastPlayerLists, context, true);

        RecyclerView fascistTracks = getView().findViewById(R.id.list_custom_tracks);
        RecyclerViewManager.initialiseSetupRecyclerView(fascistTracks, context, false);

        //Resetting view visibility
        setup_container_settings.setVisibility(View.GONE);

        //Resetting button
        btn_setup_forward.setText(context.getString(R.string.btn_continue));

        container_setup_buttons.setVisibility(View.VISIBLE);
        setup_container_new_player.setVisibility(View.VISIBLE);
        progressBar_setupSteps.setVisibility(View.VISIBLE);

        Animation progressBarAnimation = new MainActivity.ProgressBarAnimation(progressBar_setupSteps, 0, 300);
        progressBarAnimation.setDuration(500);
        progressBar_setupSteps.startAnimation(progressBarAnimation);

        applyPreferences();
    }


    private void applyPreferences() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean useFascistTrack = defaultSharedPreferences.getBoolean("fascistTrack_defaultValue", false);
        SetupFragmentManager.toggleFascistTracks(container_fascist_tracks, useFascistTrack);
        switch_enable_tracks.setChecked(useFascistTrack);

        ((Switch) getView().findViewById(R.id.switch_server)).setChecked(defaultSharedPreferences.getBoolean("server_defaultValue", true));

        boolean useSounds = defaultSharedPreferences.getBoolean("sounds_defaultValue", false);
        ((Switch) getView().findViewById(R.id.switch_execution)).setChecked(useSounds);
        ((Switch) getView().findViewById(R.id.switch_gameEnd)).setChecked(useSounds);
        ((Switch) getView().findViewById(R.id.switch_policies)).setChecked(useSounds);
    }

    private void resetValues() {
        initialiseLayout();
        page = 1;
        progressBar_value = 300;
        PlayerListManager.initialise(playerCardList, context);
        FascistTrackSelectionManager.selectedTrackIndex = -1;
        FascistTrackSelectionManager.recommendedTrackIndex = -1;
        FascistTrackSelectionManager.previousSelection = null;
        FascistTrackSelectionManager.initialise();
        FascistTrackSelectionManager.setupOfficialTrackList((LinearLayout) getView().findViewById(R.id.container_official_tracks), context);
    }

    public void initialiseLayout() {
        final View fragmentLayout = getView();

        btn_setup_back = fragmentLayout.findViewById(R.id.btn_setup_back);
        btn_setup_forward = fragmentLayout.findViewById(R.id.btn_setup_forward);
        container_setup_buttons = fragmentLayout.findViewById(R.id.setup_buttons);

        setup_container_new_player = fragmentLayout.findViewById(R.id.container_setup_add_players);
        tv_choose_from_previous_games_players = fragmentLayout.findViewById(R.id.tv_choose_old_players);

        setup_container_select_track = fragmentLayout.findViewById(R.id.container_setup_set_Track);
        switch_enable_tracks = fragmentLayout.findViewById(R.id.switch_useTrack);
        container_fascist_tracks = fragmentLayout.findViewById(R.id.container_fascist_track);
        tv_title_custom_tracks = fragmentLayout.findViewById(R.id.tv_title_custom_tracks);

        setup_container_settings = fragmentLayout.findViewById(R.id.container_setup_settings);

        playerCardList = fragmentLayout.findViewById(R.id.playerList);
        playerCardList.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        PlayerListManager.initialise(playerCardList, context);

        progressBar_setupSteps = fragmentLayout.findViewById(R.id.progressBar_setupProgress);
        progressBar_setupSteps.setMax(900);

        setupVariables(fragmentLayout);
    }


    private void setupVariables(View fragmentLayout) {
        setupContinueConditions = new SetupFragmentManager.SetupContinueCondition[] {SetupFragmentManager.firstCondition(context, SetupFragment.this), SetupFragmentManager.secondCondition(context, SetupFragment.this)};
        pages = new ConstraintLayout[] {setup_container_new_player, setup_container_select_track, setup_container_settings};

        fab_newTrack = fragmentLayout.findViewById(R.id.fab_create_custom_track);
        fab_newTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FascistTrackCreationDialog.showTrackCreationDialog(context);
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

    public void nextSetupPage(boolean forceNextPage) {
        SetupFragmentManager.SetupContinueCondition condition = null;
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

                SetupFragmentManager.animateTransition(new ConstraintLayout[] {oldPage, newPage}, new Animation[] {slideInRight, slideOutLeft}, false, SetupFragment.this);

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

            SetupFragmentManager.animateFAB(page, true, SetupFragment.this);
            SetupFragmentManager.animateTransition(new ConstraintLayout[] {oldPage, newPage}, new Animation[] {slideInLeft, slideOutRight}, false, SetupFragment.this);

            if(page == 2) btn_setup_forward.setText(getString(R.string.btn_continue));
        } else cancelSetup();
    }

    private void cancelSetup() {
        btn_setup_back.setOnClickListener(null);
        ((MainActivity) context).replaceFragment(MainActivity.page_main, true);
        progressBar_newValue = 0;
        SetupFragmentManager.animateTransition(null, null, true, SetupFragment.this);
    }

    private void finishSetup() {
        View fragmentLayout = getView();
        GameEventsManager.endSounds = ((Switch) fragmentLayout.findViewById(R.id.switch_gameEnd)).isChecked();
        GameEventsManager.policySounds = ((Switch) fragmentLayout.findViewById(R.id.switch_policies)).isChecked();
        GameEventsManager.executionSounds = ((Switch) fragmentLayout.findViewById(R.id.switch_execution)).isChecked();

        GameEventsManager.server = ((Switch) fragmentLayout.findViewById(R.id.switch_server)).isChecked();

        if(!switch_enable_tracks.isChecked()) GameManager.enableManualMode();

        ((MainActivity) context).replaceFragment(MainActivity.page_game, true);
    }
}


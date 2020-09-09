package de.tobiundmario.secrethitlermobilecompanion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrackSelectionManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.JSONManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PlayerList;

public class MainActivity extends AppCompatActivity {

    public MainScreenFragment fragment_main;
    public SetupFragment fragment_setup;
    public GameFragment fragment_game;

    private LinearLayout currentFragmentContainer;

    private LinearLayout container_main, container_setup, container_game;

    public static final int main = 0, setup = 1, game = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment_main = (MainScreenFragment) fragmentManager.findFragmentById(R.id.fragment_main);
        fragment_setup = (SetupFragment) fragmentManager.findFragmentById(R.id.fragment_setup);
        fragment_game = (GameFragment) fragmentManager.findFragmentById(R.id.fragment_game);

        container_main = findViewById(R.id.container_fragment_main);
        container_game = findViewById(R.id.container_fragment_game);
        container_setup = findViewById(R.id.container_fragment_setup);

        currentFragmentContainer = container_main;

        fragment_setup.initialiseLayout();

        GameLog.setContext(this);
        checkForBackups();

        ExceptionHandler.initialise(this);
    }

    public void replaceFragment(int fragmentNumberToReplace, boolean fade) {
        final LinearLayout oldContainer = currentFragmentContainer;

        switch (fragmentNumberToReplace) {
            case main:
                container_main.setVisibility(View.VISIBLE);
                currentFragmentContainer = container_main;
                break;
            case game:
                container_game.setVisibility(View.VISIBLE);
                currentFragmentContainer = container_game;
                fragment_game.start();
                break;
            case setup:
                container_setup.setVisibility(View.VISIBLE);
                currentFragmentContainer = container_setup;
                fragment_setup.startSetup();
                break;
        }

        if(fade) {
            oldContainer.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            oldContainer.setVisibility(View.GONE);
                        }
                    })
                    .start();

            currentFragmentContainer.setAlpha(0f);
            currentFragmentContainer.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            currentFragmentContainer.setAlpha(1f);
                        }
                    })
                    .start();
        } else {
            oldContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if(currentFragmentContainer.equals(container_setup)) {
            fragment_setup.previousPage();
        } else if(GameLog.isGameStarted()) { //Game is currently running, we ask the user if he wants to end the game
            if(!GameLog.swipeEnabled) return; //This means that the "Game Ended" Screen is currently showing, we do not want to show the dialog during this

            CardDialog.showMessageDialog(this, getString(R.string.title_end_game_policies), null, getString(R.string.yes), new Runnable() {
                @Override
                public void run() {
                    fragment_game.displayEndGameOptions();
                }
            }, getString(R.string.no), null);
        } else{ //User is in the empty screen, end the activity
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //These functions set references to MainActivity to null, as not doing this would result in a memory leak
        GameLog.destroy();
        PlayerList.destroy();
        JSONManager.destroy();
        FascistTrackSelectionManager.destroy();
        CardDialog.destroy();
        ExceptionHandler.destroy();
    }

    public void checkForBackups() {
        if(GameLog.backupPresent()) {
            CardDialog.showMessageDialog(this, getString(R.string.dialog_restore_title), getString(R.string.dialog_restore_msg), getString(R.string.yes), new Runnable() {
                @Override
                public void run() {
                    PlayerList.setContext(MainActivity.this);
                    GameLog.restoreBackup();
                    replaceFragment(game, true);
                }
            }, getString(R.string.no), new Runnable() {
                @Override
                public void run() {
                    GameLog.deleteBackup();
                }
            });
        }
    }

    public LinearLayout getCurrentFragmentContainer() {
        return currentFragmentContainer;
    }

    public static class ProgressBarAnimation extends Animation{
        private ProgressBar progressBar;
        private float from;
        private float  to;

        public ProgressBarAnimation(ProgressBar progressBar, float from, float to) {
            super();
            this.progressBar = progressBar;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            float value = from + (to - from) * interpolatedTime;
            progressBar.setProgress((int) value);
        }

    }
}
package de.tobiundmario.secrethitlermobilecompanion;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrack;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PlayerList;

public class MainActivity extends AppCompatActivity {

    private Fragment currentFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForBackups();

        replaceFragments(MainScreenFragment.class, false);

        //TODO These methods are for testing purposes only and should be removed from the onCreate function after testing
        //autoCreateGame();
    }

    public void replaceFragments(Class fragmentClass, boolean fade) {
        try {
            currentFragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        ft.replace(R.id.fragment_placeholder, currentFragment)
                .commit();
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    @Override
    public void onBackPressed() {
        //TODO add support for setup
        if(GameLog.isGameStarted()) { //Game is currently running, we ask the user if he wants to end the game
            if(!GameLog.swipeEnabled) return; //This means that the "Game Ended" Screen is currently showing, we do not want to show the dialog during this

            CardDialog.showMessageDialog(this, getString(R.string.title_end_game_policies), null, getString(R.string.yes), new Runnable() {
                @Override
                public void run() {
                    ((GameFragment) currentFragment).displayEndGameOptions();
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

    private void autoCreateGame() {
        PlayerList.addPlayer("Rüdiger");
        PlayerList.addPlayer("Markus");
        PlayerList.addPlayer("Friedrich");
        PlayerList.addPlayer("Leonard");
        PlayerList.addPlayer("Anke");
        PlayerList.addPlayer("Björn");
        PlayerList.addPlayer("Knut");
        PlayerList.addPlayer("Richard");

        //TODO make this function work again

        FascistTrack ft_78 = new FascistTrack();
        ft_78.setActions(new int[] {FascistTrack.NO_POWER, FascistTrack.INVESTIGATION, FascistTrack.SPECIAL_ELECTION, FascistTrack.EXECUTION, FascistTrack.EXECUTION});
        GameLog.gameTrack = ft_78;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //These functions set references to MainActivity to null, as not doing this would result in a memory leak
        GameLog.destroy();
        PlayerList.destroy();
    }

    public void checkForBackups() {
        if(GameLog.backupPresent(this)) {
            CardDialog.showMessageDialog(this, getString(R.string.dialog_restore_title), getString(R.string.dialog_restore_msg), getString(R.string.yes), new Runnable() {
                @Override
                public void run() {
                    GameLog.restoreBackup();
                    replaceFragments(GameFragment.class, true);
                }
            }, getString(R.string.no), new Runnable() {
                @Override
                public void run() {
                    GameLog.deleteBackup();
                }
            });
        }
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
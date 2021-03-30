package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import de.tobiundmario.secrethitlermobilecompanion.MainActivity;
import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SetupFragment;

public final class SetupFragmentManager {

    private SetupFragmentManager() {}

    public static SetupContinueCondition firstCondition(final Context context, final SetupFragment setupFragment) {
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
                    String title = (playerCount == 0) ? context.getString(R.string.no_players_added) : context.getString(R.string.title_too_little_players);
                    CardDialog.showMessageDialog(context, title, context.getString(R.string.no_players_added_msg), context.getString(R.string.btn_ok), null, null, null);
                } else {
                    CardDialog.showMessageDialog(context, context.getString(R.string.title_too_little_players), context.getString(R.string.msg_too_little_players, playerCount), context.getString(R.string.btn_continue), new Runnable() {
                        @Override
                        public void run() {
                            setupFragment.nextSetupPage(true);
                        }
                    }, context.getString(R.string.btn_cancel), null);
                }
            }

            @Override
            public void initialiseLayout() {
                //Check if there is a recommended track available
                LinearLayout container_recommended_track = setupFragment.getView().findViewById(R.id.container_recommended_track);

                if(PlayerListManager.getPlayerList().size() >=5 && PlayerListManager.getPlayerList().size() <=10) {//Recommended track available
                    container_recommended_track.setVisibility(View.VISIBLE);
                    FascistTrackSelectionManager.setupRecommendedCard(setupFragment.getLayoutInflater(), container_recommended_track);
                } else {
                    container_recommended_track.setVisibility(View.GONE);
                    FascistTrackSelectionManager.recommendedTrackIndex = -1;
                    FascistTrackSelectionManager.recommendedCard = null;
                }

                setupFragment.switch_enable_tracks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        toggleFascistTracks(setupFragment.container_fascist_tracks, b);
                    }
                });

                animateFAB(2, false, setupFragment);
            }
        };
    }

    public static void toggleFascistTracks(ConstraintLayout container, boolean enabled) {
        container.setAlpha(enabled ? 1f : 0.5f);
        container.setClickable(enabled);
        container.setEnabled(enabled);
    }

    public static SetupContinueCondition secondCondition(final Context context, final SetupFragment setupFragment) {
        //When switching from page 2 to 3
        return new SetupContinueCondition() {
            @Override
            public boolean shouldSetupContinue() {
                return GameManager.gameTrack != null || !setupFragment.switch_enable_tracks.isChecked();
            }

            @Override
            public void showErrorMessage() {
                CardDialog.showMessageDialog(context, context.getString(R.string.no_track_selected), context.getString(R.string.no_track_selected_message), context.getString(R.string.btn_ok), null, null, null);
            }

            @Override
            public void initialiseLayout() {
                setupFragment.fab_newTrack.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_close));
            }
        };
    }

    public static void animateFAB(int page, boolean back, SetupFragment setupFragment) {
        if(back) {
            if(page == 1) closeFAB(setupFragment);
            else if (page == 2) openFab(setupFragment);
        } else if(page == 2) openFab(setupFragment);
    }

    private static void openFab(SetupFragment setupFragment) {
        setupFragment.fab_newTrack.startAnimation(AnimationUtils.loadAnimation(setupFragment.getContext(), R.anim.fab_open));
    }

    private static void closeFAB(SetupFragment setupFragment) {
        setupFragment.fab_newTrack.startAnimation(AnimationUtils.loadAnimation(setupFragment.getContext(), R.anim.fab_close));
    }

    public static void animateTransition(final ConstraintLayout [] pages, Animation[] slideAnimations, boolean progressBarOnly, SetupFragment setupFragment) {
        if(!progressBarOnly) {
            final ConstraintLayout oldPage = pages[0], newPage = pages[1];
            Animation slide_in = slideAnimations[0], slide_Out = slideAnimations[1];

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

        Animation progressBarAnimation = new MainActivity.ProgressBarAnimation(setupFragment.progressBar_setupSteps, setupFragment.progressBar_value, setupFragment.progressBar_newValue);
        progressBarAnimation.setDuration(500);
        setupFragment.progressBar_setupSteps.startAnimation(progressBarAnimation);
        setupFragment.progressBar_value = setupFragment.progressBar_newValue;
    }

    public interface SetupContinueCondition {
        abstract boolean shouldSetupContinue();
        abstract void showErrorMessage();
        abstract void initialiseLayout();
    }
}

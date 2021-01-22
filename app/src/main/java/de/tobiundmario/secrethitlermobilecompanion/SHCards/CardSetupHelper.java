package de.tobiundmario.secrethitlermobilecompanion.SHCards;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.List;

import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.PlayerListManager;

public final class CardSetupHelper {

    private CardSetupHelper() {}

    public static void lockPresidentSpinner(String presidentName, Spinner spinner) {
        int position = PlayerListManager.getPlayerPosition(presidentName);
        spinner.setSelection(position);
        if(!GameManager.isManualMode()) spinner.setEnabled(false);
    }

    /**
     * Is responsible for creating the ArrayAdapter required for our Spinners during Setup. They are mainly responsible for setting the font style to Comfortaa
     * @param context
     * @param data A List of data to put in the Spinner (e.g. a list of Claims or the Player List)
     * @param claimSpinnerAdapter whether or not the Spinner contains Claims to be colored
     * @return an ArrayAdapter
     */
    public static ArrayAdapter<String> getArrayAdapter(final Context context, List<String> data, final boolean claimSpinnerAdapter) {
        return new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return createView(position, convertView, parent);
            }

            @Override
            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                return createView(position, convertView, parent);
            }

            private View createView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);

                Typeface externalFont = ResourcesCompat.getFont(context, R.font.comfortaa_light);
                tv.setTypeface(externalFont);

                if(claimSpinnerAdapter) tv.setText(Claim.colorClaim(tv.getText().toString()), TextView.BufferType.SPANNABLE);

                return tv;
            }
        };
    }


    /**
     * Creates an imageView selector, as e.g. seen in the Setup of a Legislative session. If the first image is clicked, the alpha of the second image is reduced, indicating a selection.
     * Furthermore, Colorstatelists and Runnables can be provided, leading to Views e.g. Switches being colored in a specific way if an image is selected or just general Layout changes occurring
     * @param imageViews the involved ImageViews
     * @param colorStateLists the ColorStateLists containing the color themes for each selection
     * @param coloredViews an Array containing the views that are to be colored
     * @param runnables an Array of Runnables that are run when the ImageView is selected
     */
    public static void setupImageViewSelector(ImageView[] imageViews, final ColorStateList[] colorStateLists, final View[] coloredViews, Runnable[] runnables) {
        final ImageView image1 = imageViews[0], image2 = imageViews[1];
        final ColorStateList csl1 = colorStateLists[0], csl2 = colorStateLists[1];
        final Runnable run1 = runnables[0], run2 = runnables[1];

        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image1.setAlpha(1f);
                image2.setAlpha(0.2f);

                if(csl1 != null && coloredViews != null) colorViews(csl1, coloredViews);
                if(run1 != null) run1.run();
            }
        });

        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image2.setAlpha(1f);
                image1.setAlpha(0.2f);

                if(csl2 != null && coloredViews != null) colorViews(csl2, coloredViews);
                if(run2 != null) run2.run();
            }
        });
    }

    /**
     * Colors an array of Views according to a ColorStateList
     * @param colorStateList The color that will be used
     * @param viewsToBeColored An array of views that will be colored. Supports Switches, Checkboxes and RadioButtons
     */
    private static void colorViews (ColorStateList colorStateList, View[] viewsToBeColored) {
        for(View view : viewsToBeColored) {

            if(view instanceof CheckBox) ((CheckBox) view).setButtonTintList(colorStateList);
            else if (view instanceof Switch) {
                ((Switch) view).setThumbTintList(colorStateList);
                ((Switch) view).setTrackTintList(colorStateList);
            } else if(view instanceof RadioButton) {
                ((RadioButton)view).setButtonTintList(colorStateList);
            }

        }
    }

    /**
     * Provides an easy way to create a setup with multiple pages
     * @param setupPages An Array of pages, must be in correct order
     * @param btn_continue a reference to the next page button
     * @param btn_back a reference to the previous page button
     * @param cardSetupListeners A helper class containing multiple Listeners useful for creating a setup
     */
    public static void initialiseSetupPages(final View[] setupPages, View btn_continue, final View btn_back, final CardSetupListeners cardSetupListeners) {
        changeButtonText(btn_back, GameEventsManager.getContext().getString(R.string.btn_cancel));
        final int[] setupPage = {1};

        for(int i = 0; i < setupPages.length; i++) {
            setupPages[i].setVisibility((i == 0) ? View.VISIBLE : View.GONE);
        }

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cardSetupListeners.shouldPageTransitionOccurr(setupPage[0])) {
                    setupPage[0]++;
                    if (setupPage[0] == 2)
                        changeButtonText(btn_back, GameEventsManager.getContext().getString(R.string.back));
                    nextSetupPage(setupPage, setupPages, cardSetupListeners.getSetupFinishCondition(), cardSetupListeners.getOnSetupFinishedListener());
                    cardSetupListeners.triggerPageSetup(setupPage[0] - 1, setupPages);
                }
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupPage[0]--;
                previousSetupPage(setupPage, setupPages, btn_back, cardSetupListeners.getOnSetupCancelledListener());
            }
        });
    }

    private static void nextSetupPage(int[] setupPage, View[] setupPages, SetupFinishCondition setupFinishCondition, OnSetupFinishedListener onSetupFinishedListener) {
        final View oldPage, newPage;
        if(setupPage[0] <= setupPages.length && (setupFinishCondition == null || !setupFinishCondition.shouldSetupBeFinished(setupPage[0]))) {
            oldPage = setupPages[setupPage[0] - 2];
            newPage = setupPages[setupPage[0] - 1];

            //Animations
            Animation slideOutLeft = AnimationUtils.loadAnimation(GameEventsManager.getContext(), R.anim.slide_out_left);
            Animation slideInRight = AnimationUtils.loadAnimation(GameEventsManager.getContext(), R.anim.slide_in_right);
            slideInRight.setFillAfter(true);
            animateTransition(oldPage, newPage, slideOutLeft, slideInRight);
        } else {
            onSetupFinishedListener.onSetupFinished();
        }
    }

    private static void previousSetupPage(int[] setupPage, View[] setupPages, View btn_back, OnSetupCancelledListener onSetupCancelledListener) {
        final View oldPage, newPage;
        if(setupPage[0] >= 1) {
            oldPage = setupPages[setupPage[0]];
            newPage = setupPages[setupPage[0] - 1];

            //Animations
            Animation slideOutRight = AnimationUtils.loadAnimation(GameEventsManager.getContext(), R.anim.slide_out_right);
            Animation slideInLeft = AnimationUtils.loadAnimation(GameEventsManager.getContext(), R.anim.slide_in_left);
            animateTransition(oldPage, newPage, slideOutRight, slideInLeft);

            if(setupPage[0] == 1) changeButtonText(btn_back, GameEventsManager.getContext().getString(R.string.btn_cancel));
        } else {
            if(onSetupCancelledListener != null) onSetupCancelledListener.onSetupCancelled();
        }
    }

    private static void animateTransition(final View oldPage, View newPage, Animation slide_Out, Animation slide_in) {
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

    private static void changeButtonText(View btn, String text) {
        if(btn instanceof Button) ((Button) btn).setText(text);
        else if(btn instanceof TextView) ((TextView) btn).setText(text);
    }
}
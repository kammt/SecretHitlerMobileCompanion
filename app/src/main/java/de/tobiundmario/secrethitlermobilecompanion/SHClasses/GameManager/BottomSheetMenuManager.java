package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONException;

import java.io.IOException;

import de.tobiundmario.secrethitlermobilecompanion.ExceptionHandler;
import de.tobiundmario.secrethitlermobilecompanion.GameFragment;
import de.tobiundmario.secrethitlermobilecompanion.R;
import de.tobiundmario.secrethitlermobilecompanion.SHCards.CardDialog;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.DeckShuffledEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.PolicyPeekEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.SpecialElectionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.TopPolicyPlayedEvent;

public class BottomSheetMenuManager {

    private GameFragment gameFragment;
    private Context context;

    private ConstraintLayout bottomSheetAdd;

    private BottomNavigationView bottomNavigationMenu_game;
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener;

    private BottomSheetBehavior bottomSheetBehaviorAdd;
    private BottomSheetBehavior bottomSheetBehaviorServer;
    private BottomSheetBehavior bottomSheetBehaviorGameStatus;

    private View entry_loyaltyInvestigation, entry_execution, entry_policy_peek, entry_special_election, entry_top_policy;

    public BottomSheetMenuManager(GameFragment linkedGameFragment) {
        gameFragment = linkedGameFragment;
        context = linkedGameFragment.getContext();
    }


    public void deselectAllMenuItems() {
        Menu menu = bottomNavigationMenu_game.getMenu();
        menu.getItem(0).setVisible(false);
        menu.getItem(0).setChecked(true);
    }

    public void disableMenuBar() {
        //Make the Menu non-functioning
        bottomNavigationMenu_game.getMenu().getItem(3).setCheckable(false);
        bottomNavigationMenu_game.getMenu().getItem(2).setCheckable(false);
        bottomNavigationMenu_game.getMenu().getItem(1).setCheckable(false);
        bottomNavigationMenu_game.setOnNavigationItemSelectedListener(null);
        deselectAllMenuItems();
        //Hide the BottomSheets
        bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviorGameStatus.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void enableMenuBar() {
        bottomNavigationMenu_game.getMenu().getItem(3).setCheckable(true);
        bottomNavigationMenu_game.getMenu().getItem(2).setCheckable(true);
        bottomNavigationMenu_game.getMenu().getItem(1).setCheckable(true);
        bottomNavigationMenu_game.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
    }

    public void setupBottomMenu(View fragmentLayout) {
        initialiseBottomSheetLayout(fragmentLayout);
        initialiseMenuEntries(fragmentLayout);

        //Setting up the Bottom Menu
        deselectAllMenuItems();

        //When the game ends, the Menu items are disabled. Hence, we enable them again just in case
        bottomNavigationMenu_game.getMenu().getItem(3).setCheckable(true);
        bottomNavigationMenu_game.getMenu().getItem(2).setCheckable(true);
        bottomNavigationMenu_game.getMenu().getItem(1).setCheckable(true);
        if(GameManager.isManualMode()) bottomNavigationMenu_game.getMenu().getItem(2).setVisible(false);

        setupAddEventButton();

        //Setting Up the Server Status Page
        ConstraintLayout bottomSheetServer = fragmentLayout.findViewById(R.id.bottom_sheet_server_status);
        bottomSheetBehaviorServer = BottomSheetBehavior.from(bottomSheetServer);
        bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN);
        gameFragment.getServerPaneManager().setupServerLayout(fragmentLayout);

        ConstraintLayout bottomSheetGameStatus = fragmentLayout.findViewById(R.id.bottom_sheet_game_status);
        bottomSheetBehaviorGameStatus = BottomSheetBehavior.from(bottomSheetGameStatus);
        bottomSheetBehaviorGameStatus.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Setting up the BottomSheetCallback
        setupBottomSheetCallback();

        //Adding the Listener to the BottomNavigationMenu
        setupNavigationSelection();
    }

    private void setupBottomSheetCallback() {
        BottomSheetBehavior.BottomSheetCallback deselectMenuItemsCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN && bottomSheetBehaviorServer.getState() == BottomSheetBehavior.STATE_HIDDEN && bottomSheetBehaviorAdd.getState() == BottomSheetBehavior.STATE_HIDDEN && bottomSheetBehaviorGameStatus.getState() == BottomSheetBehavior.STATE_HIDDEN) { //If the state changed to hidden (i.e. the user closed the menu), the item should now be unselected
                    deselectAllMenuItems();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //Function unneeded, as we only update the bottom sheets after the slides are completed
            }
        };
        bottomSheetBehaviorAdd.addBottomSheetCallback(deselectMenuItemsCallback);
        bottomSheetBehaviorServer.addBottomSheetCallback(deselectMenuItemsCallback);
        bottomSheetBehaviorGameStatus.addBottomSheetCallback(deselectMenuItemsCallback);
    }

    private void initialiseBottomSheetLayout(View fragmentLayout) {
        bottomNavigationMenu_game = fragmentLayout.findViewById(R.id.bottomNavigationView_game);
        bottomSheetAdd = fragmentLayout.findViewById(R.id.bottom_sheet_add_event);
    }

    private void initialiseMenuEntries(View fragmentLayout) {
        entry_loyaltyInvestigation = bottomSheetAdd.findViewById(R.id.loyalty_investigation);
        entry_execution = bottomSheetAdd.findViewById(R.id.execution);
        entry_policy_peek = bottomSheetAdd.findViewById(R.id.policy_peek);
        entry_special_election = bottomSheetAdd.findViewById(R.id.special_election);
        entry_top_policy = bottomSheetAdd.findViewById(R.id.topPolicy);
    }

    public void showEnableManualModeDialog() {
        CardDialog.showMessageDialog(context, context.getString(R.string.dialog_manual_mode_title), context.getString(R.string.dialog_manual_mode_desc), context.getString(R.string.btn_ok), new Runnable() {
            @Override
            public void run() {
                GameManager.enableManualMode();
                bottomSheetBehaviorGameStatus.setState(BottomSheetBehavior.STATE_HIDDEN);
                bottomNavigationMenu_game.getMenu().getItem(2).setVisible(false);
                setupAddEventButton();
                try {
                    BackupManager.backupToCache();
                }catch (IOException | JSONException e) {
                    ExceptionHandler.showErrorSnackbar(e, "BottomSheetMenuManager.showEnableManualModeDialog()");
                }
            }
        }, context.getString(R.string.btn_cancel), null);
    }

    private void setupAddEventButton() {
        bottomSheetBehaviorAdd = BottomSheetBehavior.from(bottomSheetAdd);
        bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);

        /*
        Setting up the OnClickListeners. For this, we get each ConstraintLayout by using fragmentLayout.findViewById
        However, we have to differentiate here, as we only want Legislative Session and Deck shuffled to be visible when manual mode is enabled
         */
        bottomSheetAdd.findViewById(R.id.legislative_session).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddEvent(new LegislativeSession(context));
            }
        });

        bottomSheetAdd.findViewById(R.id.deck_shuffled).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddEvent(new DeckShuffledEvent(context));
            }
        });

        int visibility = GameManager.isManualMode() ? View.VISIBLE : View.GONE;

        entry_loyaltyInvestigation.setVisibility(visibility);
        entry_execution.setVisibility(visibility);
        entry_policy_peek.setVisibility(visibility);
        entry_special_election.setVisibility(visibility);
        entry_top_policy.setVisibility(visibility);

        if(GameManager.isManualMode()) {
            setupManualModeAddButtons();
        }
    }

    private void setupManualModeAddButtons() {
        entry_loyaltyInvestigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddEvent(new LoyaltyInvestigationEvent(null, context));
            }
        });

        entry_execution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddEvent(new ExecutionEvent(null, context));
            }
        });

        entry_policy_peek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddEvent(new PolicyPeekEvent(null, context));
            }
        });

        entry_special_election.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddEvent(new SpecialElectionEvent(null, context));
            }
        });

        entry_top_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleAddEvent(new TopPolicyPlayedEvent(context));
            }
        });
    }

    private void handleAddEvent(GameEvent newEvent) {
        GameEventsManager.addEvent(newEvent);
        bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void setupNavigationSelection() {
        onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.navigation_add_event:
                        handleNavigationSelection(2);
                        break;
                    case R.id.navigation_server_status:
                        handleNavigationSelection(0);
                        break;
                    case R.id.navigation_game_status:
                        handleNavigationSelection(1);
                    default:
                        break;
                }
                return true;
            }
        };
        bottomNavigationMenu_game.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
    }

    private void handleNavigationSelection(int page) {
        BottomSheetBehavior newBottomSheet;
        BottomSheetBehavior[] otherBottomSheets;

        switch (page) {
            case 0: //Server Status
                newBottomSheet = bottomSheetBehaviorServer;
                otherBottomSheets = new BottomSheetBehavior[] {bottomSheetBehaviorAdd, bottomSheetBehaviorGameStatus};
                break;
            case 1: //Game Status
                newBottomSheet = bottomSheetBehaviorGameStatus;
                otherBottomSheets = new BottomSheetBehavior[] {bottomSheetBehaviorAdd, bottomSheetBehaviorServer};
                gameFragment.updateGameStatusPage();
                break;
            case 2: //Add Event
                newBottomSheet = bottomSheetBehaviorAdd;
                otherBottomSheets = new BottomSheetBehavior[] {bottomSheetBehaviorServer, bottomSheetBehaviorGameStatus};
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + page);
        }

        if(newBottomSheet.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            newBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

            //Check if the other page is open. If so, we close it
            otherBottomSheets[0].setState(BottomSheetBehavior.STATE_HIDDEN);
            otherBottomSheets[1].setState(BottomSheetBehavior.STATE_HIDDEN);

        } else newBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN); //If it is already open and the user clicks it again, it should hide
    }

    public void animateMenuBar(boolean show) {
        Animation menuTransition;
        if(show) {
            bottomNavigationMenu_game.setVisibility(View.VISIBLE);
            menuTransition = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
        } else {
            menuTransition = new TranslateAnimation(0, 0, 0, 200);
            menuTransition.setDuration(500);
        }
        bottomNavigationMenu_game.startAnimation(menuTransition);
    }

}

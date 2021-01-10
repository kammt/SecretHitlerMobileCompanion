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

import de.tobiundmario.secrethitlermobilecompanion.GameFragment;
import de.tobiundmario.secrethitlermobilecompanion.R;
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
        bottomNavigationMenu_game.getMenu().getItem(2).setCheckable(false);
        bottomNavigationMenu_game.getMenu().getItem(1).setCheckable(false);
        bottomNavigationMenu_game.setOnNavigationItemSelectedListener(null);
        deselectAllMenuItems();
        //Hide the BottomSheets
        bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehaviorAdd.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void enableMenuBar() {
        bottomNavigationMenu_game.getMenu().getItem(2).setCheckable(true);
        bottomNavigationMenu_game.getMenu().getItem(1).setCheckable(true);
        bottomNavigationMenu_game.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
    }

    public void setupBottomMenu(View fragmentLayout) {
        initialiseLayoutVariables(fragmentLayout);

        //Setting up the Bottom Menu
        deselectAllMenuItems();

        //When the game ends, the Menu items are disabled. Hence, we enable them again just in case
        bottomNavigationMenu_game.getMenu().getItem(2).setCheckable(true);
        bottomNavigationMenu_game.getMenu().getItem(1).setCheckable(true);

        setupAddEventButton();

        //Setting Up the Server Status Page
        ConstraintLayout bottomSheetServer = fragmentLayout.findViewById(R.id.bottom_sheet_server_status);
        bottomSheetBehaviorServer = BottomSheetBehavior.from(bottomSheetServer);
        bottomSheetBehaviorServer.setState(BottomSheetBehavior.STATE_HIDDEN);
        gameFragment.getServerPaneManager().setupServerLayout(fragmentLayout);

        //Setting up the BottomSheetCallback
        setupBottomSheetCallback();

        //Adding the Listener to the BottomNavigationMenu
        setupNavigationSelection();
    }

    private void setupBottomSheetCallback() {
        BottomSheetBehavior.BottomSheetCallback deselectMenuItemsCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN && bottomSheetBehaviorServer.getState() == BottomSheetBehavior.STATE_HIDDEN && bottomSheetBehaviorAdd.getState() == BottomSheetBehavior.STATE_HIDDEN) { //If the state changed to hidden (i.e. the user closed the menu), the item should now be unselected
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
    }

    private void initialiseLayoutVariables(View fragmentLayout) {
        bottomNavigationMenu_game = fragmentLayout.findViewById(R.id.bottomNavigationView_game);
        bottomSheetAdd = fragmentLayout.findViewById(R.id.bottom_sheet_add_event);

        entry_loyaltyInvestigation = bottomSheetAdd.findViewById(R.id.loyalty_investigation);
        entry_execution = bottomSheetAdd.findViewById(R.id.execution);
        entry_policy_peek = bottomSheetAdd.findViewById(R.id.policy_peek);
        entry_special_election = bottomSheetAdd.findViewById(R.id.special_election);
        entry_top_policy = bottomSheetAdd.findViewById(R.id.topPolicy);
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

        if(!GameManager.gameTrack.isManualMode()) {
            entry_loyaltyInvestigation.setVisibility(View.GONE);
            entry_execution.setVisibility(View.GONE);
            entry_policy_peek.setVisibility(View.GONE);
            entry_special_election.setVisibility(View.GONE);
            entry_top_policy.setVisibility(View.GONE);
        } else {
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
                        handleNavigationSelection(false);
                        break;
                    case R.id.navigation_server_status:
                        handleNavigationSelection(true);
                        break;
                    default:
                        break;
                }
                return true;
            }
        };
        bottomNavigationMenu_game.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
    }

    private void handleNavigationSelection(boolean serverPage) {
        BottomSheetBehavior newBottomSheet, otherBottomSheet;

        if(serverPage) {
            newBottomSheet = bottomSheetBehaviorServer;
            otherBottomSheet = bottomSheetBehaviorAdd;
        } else {
            newBottomSheet = bottomSheetBehaviorAdd;
            otherBottomSheet = bottomSheetBehaviorServer;
        }

        if(newBottomSheet.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            newBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);

            //Check if the other page is open. If so, we close it
            if(otherBottomSheet.getState() != BottomSheetBehavior.STATE_HIDDEN) otherBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

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

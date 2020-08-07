package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

import androidx.cardview.widget.CardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

public abstract class GameEvent {
    /*
        The GameEvent is a class which is inherited by all other Events that can happen in this game
        This simplifies the handling of different event types by the GameLog class.

        A Card extending this class can have three states:
            1. Normal:
                In this state, the Card simply displays the information it has. During this state, the setupCard(CardView cardview) function is called when its layout is initialised.
            2. Setup:
                In this state, the Card displays options to set the required values. During this state, isSetup is set to true and the setupSetupCard(CardView cardView) function is called when its layout is initialised
            2. Editing
                In this state, the Card has been long pressed on and now gives the user the option to edit it. During this state, isEditing is set to true, but so is isSetup. The  setupSetupCard(CardView cardView) is called as during a normal setup,
                but additionally, the setCurrentValues(CardView cardView) function is run. This function sets the current values that the Card has (e.g. president and chancellor names and their claims).
                When a card class sets permitEditing to false, the user is unable to long-click on it to edit it.

        The overall structure:
        - GameEvent
            - Legislative Session           (Uses two Helper classes to simplify its use: VoteEvent and ClaimEvent)
            - ExecutiveAction
                - LoyaltyInvestigationEvent
                - ExecutionEvent
                - PolicyPeekEvent
                - SpecialElectionEvent
            - DeckShuffledEvent
            - GameSetupCard                 (permitEditing = false)
            - GameEndCard                   (permitEditing = false)
     */

    //Setup state related
    public boolean isSetup = false;
    public abstract void setupSetupCard(CardView cardView);

    //Editing state related
    public boolean permitEditing = true;
    public boolean isEditing = false;
    public abstract void setCurrentValues(CardView cardView); //This function will be called when the user long presses on a card to edit it. It is called in after setupSetupCard() and will fill in the fields with the current values that the card has

    //Generate a unique ID for every GameEvent
    public String id = UUID.randomUUID().toString();

    //Normal state related
    public abstract void setupCard(CardView cardView); //Called by the RecyclerViewAdapter. The classes have to set up the card e.g. insert the President and Chancellor names
    public abstract boolean allInvolvedPlayersAreUnselected(ArrayList<String> unselectedPlayers); //Should return true if all involved Players are unselected (i.e. if their player card is not blurred). Necessary for event blurring, see GameLog.java for usage

    public abstract JSONObject getJSON() throws JSONException; //This function returns the JSON code of the event that is readable by the Website. This is sent over to all devices that have the website open. It is then parsed locally and displayed.

}

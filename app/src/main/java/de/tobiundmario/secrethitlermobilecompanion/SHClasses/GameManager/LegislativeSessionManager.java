package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import android.content.Context;

import de.tobiundmario.secrethitlermobilecompanion.MainActivity;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrack;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutiveAction;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.PolicyPeekEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.SpecialElectionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.TopPolicyPlayedEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.VoteEvent;

public class LegislativeSessionManager extends GameEventsManager {

    public static int legSessionNo = 1;

    /**
     * Processes changes made by the legislative session. Its functions include:
     * - update the number of policies
     * - end the game
     * - add an action defined by the FascistTrack
     * It is also called when an event has been removed
     * @param legislativeSession the event that causes changes e.g. the added event
     * @param removed if true, the event has been removed
     */
    public static void processLegislativeSession(LegislativeSession legislativeSession, boolean removed) {
        if(legislativeSession.getVoteEvent().getVotingResult() == VoteEvent.VOTE_FAILED) {
            if(gameTrack.isManualMode()) return;

            if(removed) return;
            else {
                electionTracker++;
                if(electionTracker == gameTrack.getElectionTrackerLength()) {
                    electionTracker = 0;

                    if(GameManager.isGameStarted() && legislativeSession.getPresidentAction() == null) {
                        TopPolicyPlayedEvent topPolicyPlayedEvent = new TopPolicyPlayedEvent(GameEventsManager.getContext());

                        //Link them together
                        legislativeSession.setPresidentAction(topPolicyPlayedEvent);
                        topPolicyPlayedEvent.setLinkedLegislativeSession(legislativeSession);

                        //Add it
                        addEvent(topPolicyPlayedEvent);
                    }
                }
            }
            return;
        } else electionTracker = 0;

        if(legislativeSession.getClaimEvent().isVetoed()) return;
        boolean fascist = legislativeSession.getClaimEvent().getPlayedPolicy() == Claim.FASCIST;

        if(removed && fascist) {
            fascistPolicies--;
        } else if (removed) {
            liberalPolicies--;
        } else if (fascist) {
            fascistPolicies++;

            if (fascistPolicies == gameTrack.getFasPolicies()) {
                ((MainActivity) GameEventsManager.getContext()).fragment_game.displayEndGameOptions();
            } else if(GameManager.isGameStarted() && legislativeSession.getPresidentAction() == null) addTrackAction(legislativeSession, false); //This method could also be called when a game is restored. In that case, we do not want to add new events

        } else {
            liberalPolicies++;

            if(liberalPolicies == gameTrack.getLibPolicies()) {
                ((MainActivity) GameEventsManager.getContext()).fragment_game.displayEndGameOptions();
            }
        }

    }

    /**
     * Is called when a fascist policy is played. It creates an action, if necessary
     * @param session The legislative session causing this
     * @param restorationPhase If true, the track action is added while a game is being restored. It is then added to the restoredEventList ArrayList
     */
    public static void addTrackAction(LegislativeSession session, boolean restorationPhase) {
        if(gameTrack.isManualMode()) return; //If it is set to manual mode, we abort the function as no track actions exist in that mode
        String presidentName = session.getVoteEvent().getPresidentName();

        ExecutiveAction executiveAction = null;

        Context c = GameEventsManager.getContext();

        switch (gameTrack.getAction(fascistPolicies - 1)) {
            case FascistTrack.NO_POWER:
                break;
            case FascistTrack.DECK_PEEK:
                executiveAction = new PolicyPeekEvent(presidentName, c);
                break;
            case FascistTrack.EXECUTION:
                executiveAction = new ExecutionEvent(presidentName, c);
                break;
            case FascistTrack.INVESTIGATION:
                executiveAction = new LoyaltyInvestigationEvent(presidentName, c);
                break;
            case FascistTrack.SPECIAL_ELECTION:
                executiveAction = new SpecialElectionEvent(presidentName, c);
        }
        if(executiveAction != null) {
            //Link both events together
            executiveAction.setLinkedLegislativeSession(session);
            session.setPresidentAction(executiveAction);
            //Add the new event
            if(restorationPhase) restoredEventList.add(executiveAction);
            else addEvent(executiveAction);
        }
    }



    public static void reSetSessionNumber() {
        /*
        When deleting Events, the numbers of the legislative sessions would not be correct. (e.g. if I have three sessions and I delete session 2 I want session 3 to turn into a 2) That's what this function is for
         */
        int currentSessionNumber = 1;
        for(int i = 0; i < eventList.size(); i++) {
            GameEvent event = eventList.get(i);
            if(event.getClass() == LegislativeSession.class) {//it is a legislative session
                ((LegislativeSession) event).setSessionNumber(currentSessionNumber++); //Change the session number
                RecyclerViewManager.getCardListAdapter().notifyItemChanged(i); //Update the UI
            }
        }
        legSessionNo = currentSessionNumber; //Update the global variable
    }


    /**
     * Returns the last Legislative Session
     * @return the last legislative session that was created (Highest session number) returns null if there are no legislative sessions created yet
     */
    public static LegislativeSession getLastLegislativeSession() {
        for (int i = eventList.size() - 1; i >= 0; i--) {
            GameEvent event = eventList.get(i);
            if(!event.isSetup && event instanceof LegislativeSession && ((LegislativeSession) event).getSessionNumber() == legSessionNo - 1) return (LegislativeSession) event;
        }

        return null;
    }


}

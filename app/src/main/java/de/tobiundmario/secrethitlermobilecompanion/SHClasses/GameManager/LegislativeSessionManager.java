package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import android.content.Context;

import de.tobiundmario.secrethitlermobilecompanion.MainActivity;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrack;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ClaimEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutiveAction;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.PolicyPeekEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.SpecialElectionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.TopPolicyPlayedEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.VoteEvent;

import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager.addEvent;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager.eventList;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameEventsManager.restoredEventList;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager.electionTracker;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager.fascistPolicies;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager.gameTrack;
import static de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager.liberalPolicies;

public class LegislativeSessionManager {

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
            processRejectedLegislativeSession(legislativeSession, removed);
            return;
        } else electionTracker = 0;

        if(legislativeSession.getClaimEvent().isVetoed()) return;
        updatePolicyCount(legislativeSession, removed);
    }

    private static void updatePolicyCount(LegislativeSession legislativeSession, boolean removed) {
        boolean fascist = legislativeSession.getClaimEvent().getPlayedPolicy() == Claim.FASCIST;

        if(fascist) {
            if(removed) {
                fascistPolicies--;
            } else {
                fascistPolicies++;

                if (fascistPolicies == gameTrack.getFasPolicies()) {
                    ((MainActivity) GameEventsManager.getContext()).fragment_game.displayEndGameOptions();
                } else if(GameManager.isGameStarted() && legislativeSession.getPresidentAction() == null) addTrackAction(legislativeSession, false); //This method could also be called when a game is restored. In that case, we do not want to add new events
            }
        } else {
            if(removed) {
                liberalPolicies--;
            } else {
                liberalPolicies++;

                if(liberalPolicies == gameTrack.getLibPolicies()) {
                    ((MainActivity) GameEventsManager.getContext()).fragment_game.displayEndGameOptions();
                }
            }
        }
    }

    private static void processRejectedLegislativeSession(LegislativeSession legislativeSession, boolean removed) {
        if(gameTrack.isManualMode()) return;

        if(removed) {
            electionTracker --;
        } else {
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

    /**
     * This function is called when a Legislative Session has been edited by the user. It updates variables such as the policy count, removes the presidential action etc.
     * @param legislativeSession The edited LegislativeSession
     * @param claimEvent The original claim Event
     * @param newClaimEvent The new claim Event
     * @param voteEvent The original voteEvent
     * @param newVoteEvent The new voteEvent
     */
    public static void processLegislativeSessionEdit(LegislativeSession legislativeSession, ClaimEvent claimEvent, ClaimEvent newClaimEvent, VoteEvent voteEvent, VoteEvent newVoteEvent) {
        boolean voteRejected = newVoteEvent.getVotingResult() == VoteEvent.VOTE_FAILED;

        //If we change the event to be rejected or vetoed, we reduce the policy count
        if (newVoteEvent.getVotingResult() == VoteEvent.VOTE_FAILED || newClaimEvent != null && newClaimEvent.isVetoed()) {
            if (claimEvent != null && claimEvent.getPlayedPolicy() == Claim.LIBERAL)
                liberalPolicies--;
            if (claimEvent != null && claimEvent.getPlayedPolicy() == Claim.FASCIST)
                fascistPolicies--;
        }

        //If we change the event to play a policy, we increase the policy count
        if (voteEvent.getVotingResult() == VoteEvent.VOTE_FAILED || claimEvent != null && claimEvent.isVetoed()) {
            if (newClaimEvent != null && newClaimEvent.getPlayedPolicy() == Claim.LIBERAL)
                liberalPolicies++;
            if (newClaimEvent != null && newClaimEvent.getPlayedPolicy() == Claim.FASCIST)
                fascistPolicies++;
        }

        //If we had a liberal policy and change it to a fascist policy, we update the policy count
        if (newClaimEvent != null && !newClaimEvent.isVetoed() && newClaimEvent.getPlayedPolicy() == Claim.FASCIST && claimEvent != null && !claimEvent.isVetoed() && claimEvent.getPlayedPolicy() == Claim.LIBERAL) {
            liberalPolicies--;
            fascistPolicies++;
        }

        //If we had a fascist policy and change it to a liberal policy, we update the policy count
        if (newClaimEvent != null && !newClaimEvent.isVetoed() && newClaimEvent.getPlayedPolicy() == Claim.LIBERAL && claimEvent != null && !claimEvent.isVetoed() && claimEvent.getPlayedPolicy() == Claim.FASCIST) {
            liberalPolicies++;
            fascistPolicies--;
        }

        //If we are not in manual mode, we have to recalculate the election tracker as well
        if (!gameTrack.isManualMode()) {
            //If it was rejected and now not anymore, we decrease the election tracker
            if (voteEvent.getVotingResult() == VoteEvent.VOTE_FAILED && newVoteEvent.getVotingResult() == VoteEvent.VOTE_PASSED)
                electionTracker--;
            //If it was passed and now not anymore, we increase the election tracker
            if (voteEvent.getVotingResult() == VoteEvent.VOTE_PASSED && newVoteEvent.getVotingResult() == VoteEvent.VOTE_FAILED) {
                electionTracker++;

                if (electionTracker == gameTrack.getElectionTrackerLength()) {
                    electionTracker = 0;

                    TopPolicyPlayedEvent topPolicyPlayedEvent = new TopPolicyPlayedEvent(GameEventsManager.getContext());
                    topPolicyPlayedEvent.setLinkedLegislativeSession(legislativeSession);
                    legislativeSession.setPresidentAction(topPolicyPlayedEvent);

                    addEvent(topPolicyPlayedEvent);
                }
            }
        }

        //If we are editing an event, this can cause changes. If there is a presidential action and we switch the policy to a liberal one, we have to remove the presidential action
        if (legislativeSession.getPresidentAction() != null) {
            GameEvent presidentialAction = legislativeSession.getPresidentAction();
            if (voteRejected || newClaimEvent.isVetoed() || newClaimEvent.getPlayedPolicy() == Claim.LIBERAL) {
                if (presidentialAction instanceof ExecutiveAction)
                    ((ExecutiveAction) presidentialAction).setLinkedLegislativeSession(null);
                if (presidentialAction instanceof TopPolicyPlayedEvent)
                    ((TopPolicyPlayedEvent) presidentialAction).setLinkedLegislativeSession(null);
                GameEventsManager.remove(presidentialAction);

                legislativeSession.setPresidentAction(null);
            }
        }

        //If we switch the president's name and the legislative session has a presidential action, we have to change the name in that one too
        if (!voteEvent.getPresidentName().equals(newVoteEvent.getPresidentName()) && legislativeSession.getPresidentAction() != null) {
            GameEvent presidentAction = legislativeSession.getPresidentAction();
            if (presidentAction instanceof ExecutiveAction) {
                ExecutiveAction presidentExecutiveAction = (ExecutiveAction) presidentAction;
                presidentExecutiveAction.setPresidentName(newVoteEvent.getPresidentName());

                if (presidentExecutiveAction.getPresidentName().equals(presidentExecutiveAction.getTargetName())) { //If the president and chancellor name are now the same, the user is prompted to edit that event as well
                    presidentExecutiveAction.isSetup = true;
                    presidentExecutiveAction.isEditing = true;
                    RecyclerViewManager.getCardListAdapter().notifyItemChanged(GameEventsManager.getEventList().size() - 1);
                }
            }
        }
    }

    /**
     * Returns whether a track action has to be added to an edited LegislativeSession
     * @param legislativeSession The edited LegislativeSession
     * @param claimEvent The original claim Event
     * @param newClaimEvent The new claim Event
     * @param voteEvent The original voteEvent
     * @param newVoteEvent The new voteEvent
     */
    public static boolean trackActionRequired (LegislativeSession legislativeSession, ClaimEvent claimEvent, ClaimEvent newClaimEvent, VoteEvent voteEvent, VoteEvent newVoteEvent){
        //If we switch to a fascist policy, we have to create a presidential action
        return newVoteEvent.getVotingResult() == VoteEvent.VOTE_PASSED && newClaimEvent.getPlayedPolicy() == Claim.FASCIST && !newClaimEvent.isVetoed() && (voteEvent.getVotingResult() == VoteEvent.VOTE_FAILED || claimEvent.isVetoed() || claimEvent.getPlayedPolicy() == Claim.LIBERAL);
    }
}

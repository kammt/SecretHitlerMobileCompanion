package de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.tobiundmario.secrethitlermobilecompanion.ExceptionHandler;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.EventChange;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.FascistTrack;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ClaimEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.DeckShuffledEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.ExecutionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.PolicyPeekEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.SpecialElectionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.TopPolicyPlayedEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHEvents.VoteEvent;

public final class JSONManager {
    private JSONManager() {}

    public static ConcurrentLinkedQueue<EventChange> gameLogChanges = new ConcurrentLinkedQueue<>();

    private static HashSet<String> clientIPs = new HashSet<>();

    public static void destroy() {
        gameLogChanges = null;
        clientIPs = null;
    }

    public static void initialise() {
        gameLogChanges = new ConcurrentLinkedQueue<>();
        clientIPs = new HashSet<>();
    }

    public static void setClientIPsSet(HashSet<String> clientIPsIn) {
        clientIPs = clientIPsIn;
    }

    public static String getCompleteGameJSON(String clientIP) {
        JSONObject obj = new JSONObject();
        JSONObject game = new JSONObject();

        if (clientIP != null) {
            for (EventChange change : gameLogChanges) {
                change.addClientServedTo(clientIP);
            }
        }

        try {
            game.put("players", PlayerListManager.getPlayerListJSON());
            game.put("plays", GameEventsManager.jsonData);
            obj.put("game", game);
        } catch (JSONException e) {
            ExceptionHandler.showErrorSnackbar(e, "JSONManager.getCompleteGameJSON()");
        }
        return obj.toString();
    }

    public static String getCompleteGameJSON() {
        return getCompleteGameJSON(null);
    }

    public static String getGameChangesJSON(String clientIP)  {
        // Log.v("Size gameLogChanges<>", String.valueOf(gameLogChanges.size()));
        JSONArray changesJSON = new JSONArray();

        try {
            for (EventChange change : gameLogChanges) {
                if (!change.getServedTo().contains(clientIP)) {
                    // Log.v("Serving change", change.getEvent().getJSON().toString());
                    changesJSON.put(change.serve(clientIP));
                }

                if (change.getServedTo().equals(clientIPs)) {
                    gameLogChanges.remove(change);
                    // Log.v("Removed change", change.getServedTo().toString() + "; " + change.getEvent().getJSON().toString());
                }

                // Log.v("Change served to", change.getServedTo().toString() + "; " + change.getEvent().getJSON().toString());
            }
        } catch (JSONException e) {
            ExceptionHandler.showErrorSnackbar(e, "JSONManager.getGameChangesJSON()");
        }

        return changesJSON.toString();
    }

    public static void addGameLogChange(EventChange change) {
        gameLogChanges.add(change);
    }

    public static GameEvent createGameEventFromJSON (JSONObject jsonObject, Context c) throws JSONException {
        switch ((String) jsonObject.get("type")) {
            case "legislative-session":
                return createLegislativeSessionFromJSON(jsonObject, c);
            case "shuffle":
                return createDeckShuffleFromJSON(jsonObject, c);
            case "top_policy":
                return createTopPolicyPlayedFromJSON(jsonObject, c);
            case "executive-action":
                switch ((String) jsonObject.get("executive_action_type")) {
                    case "investigate_loyalty":
                        return createLoyaltyInvestigationfromJSON(jsonObject, c);
                    case "execution":
                        return  createExecutionFromJSON(jsonObject, c);
                    case "policy_peek":
                        return createPolicyPeekFromJSON(jsonObject, c);
                    case "special_election":
                        return createSpecialElectionFromJSON(jsonObject, c);
                }
                break;
            default:
                throw new IllegalStateException("JSONObject couldn't be classified as a specific event!");
        }
        return null;
    }

    private static LegislativeSession createLegislativeSessionFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        int sessionNumber = jsonObject.getInt("num");

        String presidentName = (String) jsonObject.get("president");
        String chancellorName = (String) jsonObject.get("chancellor");
        boolean rejected = jsonObject.getBoolean("rejected");
        VoteEvent voteEvent = new VoteEvent(presidentName, chancellorName, (rejected) ? VoteEvent.VOTE_FAILED : VoteEvent.VOTE_PASSED);

        ClaimEvent claimEvent = null;
        if(!rejected) {
            int presClaim = Claim.getClaimInt((String) jsonObject.get("president_claim"));
            int chancClaim = Claim.getClaimInt((String) jsonObject.get("chancellor_claim"));

            boolean vetoed = jsonObject.getBoolean("veto");
            int policyPlayed = Claim.getClaimInt((String) jsonObject.get("policy_played"));
            claimEvent = new ClaimEvent(presClaim, chancClaim, policyPlayed, vetoed);
        }

        LegislativeSession session = new LegislativeSession(voteEvent, claimEvent, c);
        session.setSessionNumber(sessionNumber);
        return  session;
    }

    private static DeckShuffledEvent createDeckShuffleFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        int liberalPolicies = jsonObject.getInt("liberal_policies");
        int fascistPolicies = jsonObject.getInt("fascist_policies");

        return new DeckShuffledEvent(liberalPolicies, fascistPolicies, c);
    }

    private static LoyaltyInvestigationEvent createLoyaltyInvestigationfromJSON(JSONObject jsonObject, Context c) throws JSONException {
        String presidentName = (String) jsonObject.get("president");
        String target = (String) jsonObject.get("target");

        int claim = Claim.getClaimInt((String) jsonObject.get("claim"));
        return new LoyaltyInvestigationEvent(presidentName, target, claim, c);
    }

    private static ExecutionEvent createExecutionFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        String presidentName = (String) jsonObject.get("president");
        String target = (String) jsonObject.get("target");

        return new ExecutionEvent(presidentName, target, c);
    }

    private static PolicyPeekEvent createPolicyPeekFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        String presidentName = (String) jsonObject.get("president");
        int claim = Claim.getClaimInt((String) jsonObject.get("claim"));

        return new PolicyPeekEvent(presidentName, claim, c);
    }

    private static SpecialElectionEvent createSpecialElectionFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        String presidentName = (String) jsonObject.get("president");
        String target = (String) jsonObject.get("target");

        return new SpecialElectionEvent(presidentName, target, c);
    }

    private static TopPolicyPlayedEvent createTopPolicyPlayedFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        return new TopPolicyPlayedEvent(Claim.getClaimInt((String) jsonObject.get("policy_played")), c);
    }

    public static JSONObject writeFascistTrackToJSON(FascistTrack fascistTrack) throws JSONException {
        JSONObject object = new JSONObject();

        object.put("name", fascistTrack.getName());

        int[] actions = fascistTrack.getActions();
        JSONArray actionsArray = new JSONArray();
        for(int action : actions) {
            actionsArray.put(action);
        }
        object.put("manual", false);
        object.put("actions", actionsArray);

        object.put("electionTracker", fascistTrack.getElectionTrackerLength());

        object.put("fpolicies", fascistTrack.getFasPolicies());
        object.put("lpolicies", fascistTrack.getLibPolicies());

        return object;
    }

    public static FascistTrack restoreFascistTrackFromJSON(JSONObject object) throws JSONException {
        FascistTrack track = new FascistTrack();

        if(object.has("name")) {
            track.setName(object.getString("name"));
        }

        int fasPolicies = object.getInt("fpolicies");

        track.setFasPolicies(fasPolicies);
        track.setLibPolicies(object.getInt("lpolicies"));

        JSONArray actionsArray = object.getJSONArray("actions");
        int[] actions = new int[fasPolicies];
        for(int i = 0; i < actionsArray.length(); i++) {
            actions[i] = actionsArray.getInt(i);
        }
        track.setActions(actions);

        track.setElectionTrackerLength(object.getInt("electionTracker"));

        return track;
    }
}

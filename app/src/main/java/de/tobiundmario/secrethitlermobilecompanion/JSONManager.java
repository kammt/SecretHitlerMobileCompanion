package de.tobiundmario.secrethitlermobilecompanion;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import de.tobiundmario.secrethitlermobilecompanion.SHClasses.Claim;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.ClaimEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.DeckShuffledEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.ExecutionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameLog;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.LegislativeSession;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.LoyaltyInvestigationEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PlayerList;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.PolicyPeekEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.SpecialElectionEvent;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.VoteEvent;

public class JSONManager {
    public static String getJSON() {
        JSONObject obj = new JSONObject();
        JSONObject game = new JSONObject();

        try {
            game.put("players", PlayerList.getPlayerListJSON());
            game.put("plays", GameLog.getEventsJSON());
            obj.put("game", game);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    public static GameEvent createGameEventFromJSON (JSONObject jsonObject, Context c) throws JSONException {
        switch ((String) jsonObject.get("type")) {
            case "legislative-session":
                return createLegislativeSessionFromJSON(jsonObject, c);
            case "shuffle":
                return createDeckShuffleFromJSON(jsonObject, c);
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

        LegislativeSession session = new LegislativeSession(voteEvent, claimEvent, c, false);
        session.setSessionNumber(sessionNumber);
        return  session;
    }

    private static DeckShuffledEvent createDeckShuffleFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        int liberalPolicies = jsonObject.getInt("liberal_policies");
        int fascistPolicies = jsonObject.getInt("fascist_policies");

        return new DeckShuffledEvent(liberalPolicies, fascistPolicies, c, false);
    }

    private static LoyaltyInvestigationEvent createLoyaltyInvestigationfromJSON(JSONObject jsonObject, Context c) throws JSONException {
        String presidentName = (String) jsonObject.get("president");
        String target = (String) jsonObject.get("target");

        int claim = Claim.getClaimInt((String) jsonObject.get("claim"));
        return new LoyaltyInvestigationEvent(presidentName, target, claim, c, false);
    }

    private static ExecutionEvent createExecutionFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        String presidentName = (String) jsonObject.get("president");
        String target = (String) jsonObject.get("target");

        return new ExecutionEvent(presidentName, target, c, false);
    }

    private static PolicyPeekEvent createPolicyPeekFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        String presidentName = (String) jsonObject.get("president");
        int claim = Claim.getClaimInt((String) jsonObject.get("claim"));

        return new PolicyPeekEvent(presidentName, claim, c, false);
    }

    private static SpecialElectionEvent createSpecialElectionFromJSON(JSONObject jsonObject, Context c) throws JSONException {
        String presidentName = (String) jsonObject.get("president");
        String target = (String) jsonObject.get("target");

        return new SpecialElectionEvent(presidentName, target, c, false);
    }
}

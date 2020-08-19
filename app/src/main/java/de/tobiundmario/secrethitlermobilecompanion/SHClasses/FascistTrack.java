package de.tobiundmario.secrethitlermobilecompanion.SHClasses;

public class FascistTrack {

    public static final int NO_POWER = 0;
    public static final int EXECUTION = 1;
    public static final int INVESTIGATION = 2;
    public static final int DECK_PEEK = 3;
    public static final int SPECIAL_ELECTION = 4;


    private int[] actions = new int[5];
    private int fasPolicies = 6;
    private int libPolicies = 5;

    private int minPlayers;
    private int maxPlayers;

    private int hzStartingPolicy = 4;
    private int vetoStartingPolicy = 5;

    public FascistTrack(int minPlayers, int maxPlayers) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public void setActions(int[] actions) {
        this.actions = actions;
    }

    public int[] getActions() {
        return actions;
    }

    public void setAction(int position, int action) {
        actions[position] = action;
    }

    public int getAction(int position) {
        return actions[position];
    }

    public void setHZStartingPolicy(int HZStartingPolicy) {
        this.hzStartingPolicy = HZStartingPolicy;
    }

    public int getHzStartingPolicy() {
        return hzStartingPolicy;
    }

    public void setVetoStartingPolicy(int vetoStartingPolicy) {
        this.vetoStartingPolicy = vetoStartingPolicy;
    }

    public int getVetoStartingPolicy() {
        return vetoStartingPolicy;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setLibPolicies(int libPolicies) {
        this.libPolicies = libPolicies;
    }

    public int getLibPolicies() {
        return libPolicies;
    }

    public void setFasPolicies(int fasPolicies) {
        this.fasPolicies = fasPolicies;

        //Add another entry to the array or remove one
        int[] oldArray = actions;
        actions = new int[fasPolicies];

        for (int i = 0; i < actions.length; i++) {
            try {
                actions[i] = oldArray[i];
            }catch (ArrayIndexOutOfBoundsException e) {
                break;
            }
        }
    }

    public int getFasPolicies() {
        return fasPolicies;
    }


}

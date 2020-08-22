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

    private boolean manualMode = false;

    String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isManualMode() {
        return manualMode;
    }

    public void setManualMode(boolean manualMode) {
        this.manualMode = manualMode;
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

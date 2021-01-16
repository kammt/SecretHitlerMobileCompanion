package de.tobiundmario.secrethitlermobilecompanion.SHCards;

import android.view.View;

public class CardSetupListeners {

    private SetupPageOpenedListener[] setupPageOpenedListeners;
    private SetupFinishCondition setupFinishCondition;
    private OnSetupFinishedListener onSetupFinishedListener;
    private OnSetupCancelledListener onSetupCancelledListener;

    public void setOnSetupCancelledListener(OnSetupCancelledListener onSetupCancelledListener) {
        this.onSetupCancelledListener = onSetupCancelledListener;
    }

    public void setOnSetupFinishedListener(OnSetupFinishedListener onSetupFinishedListener) {
        this.onSetupFinishedListener = onSetupFinishedListener;
    }

    public void setSetupFinishCondition(SetupFinishCondition setupFinishCondition) {
        this.setupFinishCondition = setupFinishCondition;
    }

    public void setSetupPageOpenedListeners(SetupPageOpenedListener[] setupPageOpenedListeners) {
        this.setupPageOpenedListeners = setupPageOpenedListeners;
    }

    public OnSetupCancelledListener getOnSetupCancelledListener() {
        return onSetupCancelledListener;
    }

    public OnSetupFinishedListener getOnSetupFinishedListener() {
        return onSetupFinishedListener;
    }

    public SetupFinishCondition getSetupFinishCondition() {
        return setupFinishCondition;
    }

    public boolean shouldPageTransitionOccurr(int page) {
        if(!setupPageOpenedListenerExists(page)) return true;
        else {
            return setupPageOpenedListeners[page].shouldSetupPageBeOpened(page);
        }
    }

    public void triggerPageSetup(int page, View[] views) {
        if(setupPageOpenedListenerExists(page)) {
            setupPageOpenedListeners[page].onSetupPageOpened(page, views[page]);
        }
    }

    private boolean setupPageOpenedListenerExists(int page) {
        return !(setupPageOpenedListeners == null || page >= setupPageOpenedListeners.length || setupPageOpenedListeners[page] == null);
    }
}

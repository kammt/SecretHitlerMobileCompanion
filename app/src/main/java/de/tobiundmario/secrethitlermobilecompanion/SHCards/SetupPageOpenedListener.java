package de.tobiundmario.secrethitlermobilecompanion.SHCards;

import android.view.View;

public interface SetupPageOpenedListener {
    /**
     * Is called when a new Setup page is about to be opened.
     * @param pageNumber The page number the setup will move to. Starts at 1
     * @param pageView The view Object of the page to-be-shown
     * @return whether or not the new page should be shown, i.e. moving to this page can be blocked, for example when values are invalid
     */
    public abstract boolean onSetupPageOpened(int pageNumber, View pageView);
}

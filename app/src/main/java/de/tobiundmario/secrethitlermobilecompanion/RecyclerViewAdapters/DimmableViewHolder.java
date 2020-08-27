package de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class DimmableViewHolder extends RecyclerView.ViewHolder {
    /*
    This is the ViewHolder class used by both EventCardRecyclerViewAdapter and PlayerCardRecyclerViewAdapter.
    It has its own float value which stores the current alpha value that is should have.
    This is used by the Modified DefaultItemAnimator to restore the alpha value that the cardView has previously on
     */
    CardView cv;
    float alpha = 1;

    public DimmableViewHolder(@NonNull View itemView) {
        super(itemView);
        this.cv = (CardView) itemView;
    }
}

package de.tobiundmario.secrethitlermobilecompanion.RecyclerViewAdapters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class DimmableViewHolder extends RecyclerView.ViewHolder {
    CardView cv;
    float alpha = 1;

    public DimmableViewHolder(@NonNull View itemView) {
        super(itemView);
        this.cv = (CardView) itemView;
    }
}

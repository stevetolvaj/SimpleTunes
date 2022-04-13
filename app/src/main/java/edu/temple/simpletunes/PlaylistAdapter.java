package edu.temple.simpletunes;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private String[] trackNames;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView numTextView;
        private final TextView nameTextView;


        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            numTextView = view.findViewById(R.id.trackNumTextView);
            nameTextView = view.findViewById(R.id.trackTitleTextView);
        }

        public TextView getNameTextView() {
            return nameTextView;
        }

        public TextView getNumTextView() {
            return numTextView;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public PlaylistAdapter(String[] dataSet) {
        trackNames = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.track_holder_layout, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getNameTextView().setText(trackNames[position]);
        Log.d("TEST", "onBindViewHolder:" + position);
        // Using locale for string translation in different locales.
        String trackNum = "Track#";
        viewHolder.getNumTextView().setText(String.format(Locale.getDefault(),"%s%d", trackNum, position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return trackNames.length;
    }
}
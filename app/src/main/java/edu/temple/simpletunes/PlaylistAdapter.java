package edu.temple.simpletunes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * The PlaylistAdapter class is used to control the views within the RecyclerView and allows
 * new tracks to be shown and selected.
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private final List<String> trackNames = new ArrayList<>();
    private final Context mContext;
    private final MainActivity.OnClickInterface mOnClickInterface;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView numTextView;
        private final TextView nameTextView;


        /**
         * Initialize the views within the parent view.
         * @param view The parent view.
         */
        public ViewHolder(View view) {
            super(view);
            numTextView = view.findViewById(R.id.trackNumTextView);
            nameTextView = view.findViewById(R.id.trackTitleTextView);
        }

        /**
         * The getNameTextView method.
         * @return The nameTextView
         */
        public TextView getNameTextView() {
            return nameTextView;
        }

        /**
         * The getNumTextView method.
         * @return The numTextView
         */
        public TextView getNumTextView() {
            return numTextView;
        }
    }

    /**
     * Initialize the Context, dataset, and interface for returning position of selections.
     * @param context Context used for string resources.
     * @param data The list containing data to show in the RecyclerView.
     * @param onClickInterface The interface to return position of items selected.
     */
    public PlaylistAdapter(Context context, List<String> data, MainActivity.OnClickInterface onClickInterface) {
        this.mContext = context;
        this.trackNames.addAll(data);
        this.mOnClickInterface = onClickInterface;
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

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
            viewHolder.itemView.setOnClickListener(v -> mOnClickInterface.itemClicked(viewHolder.getAdapterPosition()));

            viewHolder.getNameTextView().setText(trackNames.get(position));
            viewHolder.getNumTextView().setText(String.format(mContext.getString(R.string.trackNum), position + 1));


    }

    @Override
    public int getItemCount() {
        return trackNames.size();
    }

}
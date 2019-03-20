package com.example.uberj.morsepocketpro;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class TrainingActivityAdapter extends RecyclerView.Adapter<TrainingActivityAdapter.TrainingActivityViewHolder> {
    private List<TrainingCardData> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class TrainingActivityViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout mLinearLayout;
        public TrainingActivityViewHolder(LinearLayout v) {
            super(v);
            mLinearLayout = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TrainingActivityAdapter (List<TrainingCardData> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TrainingActivityAdapter.TrainingActivityViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_row, parent, false);
        TrainingActivityViewHolder vh = new TrainingActivityViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TrainingActivityViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.mLinearLayout.setText(mDataset.get(position).getName());

        TrainingCardData trainingActivity = mDataset.get(position);
        TextView activityName = holder.mLinearLayout.findViewById(R.id.activityName);
        activityName.setText(trainingActivity.getName());
        TextView activityWhatDescription = holder.mLinearLayout.findViewById(R.id.activityWhatDescription);
        activityWhatDescription.setText(trainingActivity.getWhatDescription());
        TextView activityWhyDescription = holder.mLinearLayout.findViewById(R.id.activityWhyDescription);
        activityWhyDescription.setText(trainingActivity.getWhyDescription());
        RelativeLayout relativeLayout = holder.mLinearLayout.findViewById(R.id.activityCard);
        relativeLayout.setOnClickListener((view) -> view.getContext().startActivity(trainingActivity.getOnClickIntent()));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}


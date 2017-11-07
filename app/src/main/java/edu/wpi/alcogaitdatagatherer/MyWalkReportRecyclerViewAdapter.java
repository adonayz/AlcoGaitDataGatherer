package edu.wpi.alcogaitdatagatherer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import edu.wpi.alcogaitdatagatherer.WalkReportFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Walk} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyWalkReportRecyclerViewAdapter extends RecyclerView.Adapter<MyWalkReportRecyclerViewAdapter.ViewHolder> {

    private final List<Walk> walks;
    private final OnListFragmentInteractionListener mListener;

    public MyWalkReportRecyclerViewAdapter(List<Walk> walks, OnListFragmentInteractionListener listener) {
        this.walks = walks;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_walkreport, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.walk = walks.get(position);
        holder.walkNumberView.setText("Walk " + holder.walk.getWalkNumber());
        holder.walkDetailsView.setText(holder.walk.getSampleSize() + " data samples");

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.walk);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return walks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView walkNumberView;
        final TextView walkDetailsView;
        final CheckBox checkBox;
        Walk walk;

        ViewHolder(View view) {
            super(view);
            mView = view;
            walkNumberView = (TextView) view.findViewById(R.id.fragmentListWalkNumber);
            walkDetailsView = (TextView) view.findViewById(R.id.fragmentListWalkDetails);
            checkBox = (CheckBox) view.findViewById(R.id.walkSelectionCheckBox);
        }

        @Override
        public String toString() {
            return super.toString() + " 'Walk " + walk.getWalkNumber() + "'";
        }
    }
}

package edu.wpi.alcogaitdatagatherer;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class MyWalkReportRecyclerViewAdapter extends RecyclerView.Adapter<MyWalkReportRecyclerViewAdapter.ViewHolder> implements CompoundButton.OnCheckedChangeListener {

    private final List<Walk> walks;
    private final WalkReportFragment.ReportFragmentListener mListener;
    private SparseBooleanArray checkBoxStates;
    private EditText walkReportMessageInput;

    public MyWalkReportRecyclerViewAdapter(List<Walk> walks, WalkReportFragment.ReportFragmentListener listener) {
        this.walks = walks;
        mListener = listener;
        checkBoxStates = new SparseBooleanArray((walks.size()));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == R.layout.fragment_walkreport) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_walkreport, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.report_text_box, parent, false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // If view is not the text box (EditText)
        if (!(position == walks.size())) {
            holder.walk = walks.get(position);
            holder.walkNumberView.setText("Walk " + holder.walk.getWalkNumber());
            holder.walkDetailsView.setText(holder.walk.getSampleSize() + " data samples");
            holder.checkBox.setTag(position);
            holder.checkBox.setChecked(checkBoxStates.get(position, false));
            holder.checkBox.setOnCheckedChangeListener(this);
        }
    }

    @Override
    public int getItemCount() {
        return walks.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == walks.size()) ? R.layout.report_text_box : R.layout.fragment_walkreport;
    }

    public boolean isChecked(int position) {
        return checkBoxStates.get(position, false);
    }

    public void setChecked(int position, boolean isChecked) {
        checkBoxStates.put(position, isChecked);

    }

    public void toggle(int position) {
        setChecked(position, !isChecked(position));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        checkBoxStates.put((Integer) compoundButton.getTag(), b);
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
            walkReportMessageInput = (EditText) view.findViewById(R.id.walkReportMessageInput);
        }

        @Override
        public String toString() {
            return super.toString() + " 'Walk " + walk.getWalkNumber() + "'";
        }
    }

    public void saveReport() {
        mListener.submitReport(checkBoxStates, walkReportMessageInput.getText().toString());
    }
}

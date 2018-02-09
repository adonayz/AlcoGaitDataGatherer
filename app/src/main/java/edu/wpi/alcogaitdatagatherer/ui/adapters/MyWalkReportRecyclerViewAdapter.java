package edu.wpi.alcogaitdatagatherer.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedList;

import edu.wpi.alcogaitdatagatherer.R;
import edu.wpi.alcogaitdatagatherer.models.TestSubject;
import edu.wpi.alcogaitdatagatherer.ui.fragments.WalkReportFragment;

public class MyWalkReportRecyclerViewAdapter extends RecyclerView.Adapter<MyWalkReportRecyclerViewAdapter.ViewHolder> implements CompoundButton.OnCheckedChangeListener {

    private final TestSubject testSubject;
    private final WalkReportFragment.ReportFragmentListener mListener;
    private LinkedList<Boolean> checkBoxStates;
    private EditText walkReportMessageInput;

    public MyWalkReportRecyclerViewAdapter(TestSubject testSubject, WalkReportFragment.ReportFragmentListener listener) {
        this.testSubject = testSubject;
        mListener = listener;
        checkBoxStates = testSubject.getBooleanWalksList();
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
        if (!(position == checkBoxStates.size())) {
            holder.walkNumber = position + 1 + (testSubject.getStartingWalkNumber() - 1);
            holder.walkNumberView.setText("Walk " + holder.walkNumber);
            holder.walkDetailsView.setText(testSubject.getSampleSizeMap().get(holder.walkNumber) + " data samples");
            holder.checkBox.setTag(position);
            holder.checkBox.setChecked(checkBoxStates.get(position));
            holder.checkBox.setOnCheckedChangeListener(this);
        }
    }

    @Override
    public int getItemCount() {
        return checkBoxStates.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == checkBoxStates.size()) ? R.layout.report_text_box : R.layout.fragment_walkreport;
    }

    public boolean isChecked(int position) {
        return checkBoxStates.get(position);
    }

    public void setChecked(int position, boolean isChecked) {
        checkBoxStates.set(position, isChecked);
    }

    public void toggle(int position) {
        setChecked(position, !isChecked(position));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        checkBoxStates.set((Integer) compoundButton.getTag(), b);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView walkNumberView;
        final TextView walkDetailsView;
        final CheckBox checkBox;
        int walkNumber;

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
            return super.toString() + " 'Walk " + walkNumber + "'";
        }
    }

    public void saveReport() {
        mListener.submitReport(checkBoxStates, walkReportMessageInput.getText().toString());
    }
}

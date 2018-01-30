package edu.wpi.alcogaitdatagatherer.ui.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.LinkedList;

import edu.wpi.alcogaitdatagatherer.R;
import edu.wpi.alcogaitdatagatherer.models.TestSubject;
import edu.wpi.alcogaitdatagatherer.ui.activities.DataGatheringActivity;
import edu.wpi.alcogaitdatagatherer.ui.adapters.MyWalkReportRecyclerViewAdapter;

public class WalkReportFragment extends DialogFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private ReportFragmentListener mListener;
    private TestSubject testSubject;

    private AppCompatTextView saveButton;
    private AppCompatTextView cancelButton;

    public WalkReportFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static WalkReportFragment newInstance(int columnCount) {
        WalkReportFragment fragment = new WalkReportFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            testSubject = (TestSubject) getArguments().getSerializable(DataGatheringActivity.TB_FOR_WALK_REPORT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_walkreport_list, container, false);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewList);
        Context context = recyclerView.getContext();

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        recyclerView.setAdapter(new MyWalkReportRecyclerViewAdapter(testSubject, mListener));

        saveButton = view.findViewById(R.id.reportSave);
        cancelButton = view.findViewById(R.id.reportCancel);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MyWalkReportRecyclerViewAdapter) recyclerView.getAdapter()).saveReport();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ReportFragmentListener) {
            mListener = (ReportFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface ReportFragmentListener {
        void submitReport(LinkedList<Boolean> checkBoxStates, String reportMessage);
    }
}

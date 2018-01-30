package edu.wpi.alcogaitdatagatherer.ui.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import edu.wpi.alcogaitdatagatherer.interfaces.BoxUploadProgressListener;
import edu.wpi.alcogaitdatagatherer.R;

/**
 * Created by Adonay on 10/24/2017.
 */

public class CustomSurveyView extends RelativeLayout implements BoxUploadProgressListener {
    View rootView;
    LinearLayout linearLayout;
    TextView fileIDTextView;
    TextView dateModifiedTextView;
    DonutProgress donutProgress;

    public CustomSurveyView(Context context) {
        super(context);
        init(context);
    }

    public CustomSurveyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.custom_survey_view, this);
        linearLayout = rootView.findViewById(R.id.fileInfoLayout);
        fileIDTextView = rootView.findViewById(R.id.fileIDTextView);
        dateModifiedTextView = rootView.findViewById(R.id.dateModifiedTextView);
        donutProgress = rootView.findViewById(R.id.fileUploadProgressBar);
    }

    @Override
    public void onProgressUpdate(int progress) {
        donutProgress.setVisibility(View.VISIBLE);
        ObjectAnimator anim = ObjectAnimator.ofInt(donutProgress, "progress", donutProgress.getProgress(), progress);
        donutProgress.setProgress(progress);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(100);
        anim.start();
    }

    public void displayUploadComplete(){
        int i = this.indexOfChild(donutProgress);
        this.removeView(donutProgress);
        ImageView uploadCompletedLogo = new ImageView(getContext());
        uploadCompletedLogo.setImageResource(R.drawable.ic_upload_success);
        RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        newParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        uploadCompletedLogo.setLayoutParams(newParams);
        this.addView(uploadCompletedLogo, i);
    }

    public void displayUploadError(){
        int i = this.indexOfChild(donutProgress);
        this.removeView(donutProgress);
        ImageView uploadCompletedLogo = new ImageView(getContext());
        uploadCompletedLogo.setImageResource(R.drawable.ic_upload_error);
        RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        newParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        uploadCompletedLogo.setLayoutParams(newParams);
        this.addView(uploadCompletedLogo, i);
    }

    public LinearLayout getLinearLayout() {
        return linearLayout;
    }

    public TextView getFileIDTextView() {
        return fileIDTextView;
    }

    public TextView getDateModifiedTextView() {
        return dateModifiedTextView;
    }

    public DonutProgress getDonutProgress() {
        return donutProgress;
    }
}

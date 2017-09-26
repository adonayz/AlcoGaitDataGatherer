package edu.wpi.alcogaitdatagatherer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

import it.sephiroth.android.library.tooltip.Tooltip;

public class DataGatheringActivity extends AppCompatActivity {

    private String mFilename;
    private GaitRecorder gaitRecorder;
    static boolean areAllSessionsCompleted = false;
    private boolean isFirstTime = true;
    private final int RECORD_TIME_IN_SECONDS= 30;
    String subjectID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_gathering);

        Toolbar toolbar = (Toolbar) findViewById(R.id.survey_toolbar);
        toolbar.setTitle("Record Gait Data");
        setSupportActionBar(toolbar);

        subjectID = "";

        Intent prevIntent = getIntent();
        subjectID = prevIntent.getStringExtra("subject_id");

        TextView title = (TextView) findViewById(R.id.title);
        title.setText("Walk in a straight line");

        TextView text = (TextView) findViewById(R.id.summary);
        text.setText("Walk for 30 seconds. 15 seconds forward and 15 seconds back.");

        final TextView countdown = (TextView) findViewById(R.id.countdown);
        countdown.setText(Integer.toString(RECORD_TIME_IN_SECONDS));

        final TextView countdown_title = (TextView) findViewById(R.id.countdown_title);

        final Button beginButton = (Button) findViewById(R.id.begin_recording);

        final Button stopButton = (Button) findViewById(R.id.stop_recording);

        final TextView accDataDisplay = (TextView) findViewById(R.id.accDataDisplay);

        final TextView gyroDataDisplay = (TextView) findViewById(R.id.gyroDataDisplay);

        final TextView countAttemptsTextView = (TextView) findViewById(R.id.attemptsRecorded);

        final EditText bacInput = (EditText) findViewById(R.id.bacInput);

        final AppCompatTextView restartButton = (AppCompatTextView) findViewById(R.id.restartButton);

        final AppCompatTextView saveButton = (AppCompatTextView) findViewById(R.id.saveButton);

        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "/AlcoGaitDataGatherer/ID_" + subjectID.trim() + ".csv";
        mFilename = baseDir + File.separator + fileName;

        // TODO: set onClick listener

        beginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isFirstTime){
                    isFirstTime = false;
                    gaitRecorder = new GaitRecorder(DataGatheringActivity.this, mFilename, accDataDisplay, gyroDataDisplay, countAttemptsTextView);
                    gaitRecorder.registerListeners();
                }

                gaitRecorder.startRecording();
                // 3
                beginButton.setVisibility(View.GONE);
                bacInput.setEnabled(false);
                countdown_title.setVisibility(View.VISIBLE);
                countAttemptsTextView.setVisibility(View.VISIBLE);
                restartButton.setEnabled(false);
                saveButton.setEnabled(false);

                // 4
                final CountDownTimer count = new CountDownTimer(RECORD_TIME_IN_SECONDS * 1000, 1000) {

                    // 5
                    public void onTick(long millisUntilFinished) {
                        countdown.setText(String.valueOf(millisUntilFinished / 1000));
                    }

                    // 6
                    public void onFinish() {
                        stopCurrentSession(countdown_title, beginButton, stopButton, this);
                        bacInput.setEnabled(true);
                        bacInput.setText("");
                        bacInput.setHint("Update BAC");
                        createToolTip(bacInput, Tooltip.Gravity.RIGHT, "Update BAC to add walks");
                        restartButton.setEnabled(true);
                        saveButton.setEnabled(true);
                    }
                };

                // 7
                count.start();

                stopButton.setVisibility(View.VISIBLE);
                stopButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        count.cancel();
                        count.onFinish();
                    }
                });

                restartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gaitRecorder.restartDataCollection(bacInput, beginButton);
                        count.onTick(RECORD_TIME_IN_SECONDS * 1000);
                    }
                });

                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestPermissions();

                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        gaitRecorder.saveSessions();/*
                                        Intent intent = new Intent(DataGatheringActivity.this, HomeActivity.class);
                                        startActivity(intent);*/
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(DataGatheringActivity.this);
                        builder.setMessage("Do you want to complete survey and save data to a CSV file?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    }
                });
            }
        });


        bacInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = bacInput.getText().toString().trim();
                beginButton.setEnabled(!input.equals(""));
            }
        });
    }

    @Override
    protected void onStop(){
        super.onStop();
        gaitRecorder.unregisterListeners();
    }


    private void stopCurrentSession(TextView countdown_title, Button beginButton, Button stopButton, CountDownTimer countDownTimer){
        countdown_title.setVisibility(View.GONE);
        beginButton.setVisibility(View.VISIBLE);
        beginButton.setText("Add Data");
        stopButton.setVisibility(View.GONE);
        countDownTimer.onTick(RECORD_TIME_IN_SECONDS * 1000);

        gaitRecorder.stopRecording();
    }


    /**
     * Creates tooltips on the screen to guide the user through the application.
     * @param view View that the tooltip will be attached (pointing towards to).
     * @param gravity Specifies the position the tooltip will be placed relative to the attached view.
     * @param text The text that will be siplayed as a message on the tooltip.
     */
    public void createToolTip(View view, Tooltip.Gravity gravity, String text){
        Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(view, gravity)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), 3000)
                        .activateDelay(800)
                        .showDelay(300)
                        .text(text)
                        .maxWidth(700)
                        .withArrow(true)
                        .withOverlay(true)
                        .withStyleId(R.style.ToolTipLayoutCustomStyle)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .build()
        ).show();
    }

    public void requestPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        102);
            }
        }

    }
}

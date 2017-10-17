package edu.wpi.alcogaitdatagatherer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

import it.sephiroth.android.library.tooltip.Tooltip;

public class DataGatheringActivity extends AppCompatActivity {

    private String mFilename;
    private GaitRecorder gaitRecorder;
    private final int RECORD_TIME_IN_SECONDS= 60;
    private TestSubject testSubject;
    private Double BAC;
    private static final int READ_WRITE_PERMISSION_CODE = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_gathering);

        Toolbar toolbar = (Toolbar) findViewById(R.id.survey_toolbar);
        toolbar.setTitle("Record Gait Data");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent prevIntent = getIntent();
        testSubject= (TestSubject) prevIntent.getSerializableExtra("test_subject");

        TextView title = (TextView) findViewById(R.id.title);
        title.setText("Walk in a straight line");

        TextView text = (TextView) findViewById(R.id.summary);
        text.setText("Walk for 1 minute. 30 seconds forward and 30 seconds back. The phone will beep and vibrate at the 30 second mark.");

        final TextView countdown = (TextView) findViewById(R.id.countdown);
        countdown.setText(Integer.toString(RECORD_TIME_IN_SECONDS));

        final TextView countdown_title = (TextView) findViewById(R.id.countdown_title);

        final Button beginButton = (Button) findViewById(R.id.begin_recording);

        final Button stopButton = (Button) findViewById(R.id.stop_recording);

        final TextView walkNumberDisplay = (TextView) findViewById(R.id.walkNumberDisplay);

        final EditText bacInput = (EditText) findViewById(R.id.bacInput);

        final AppCompatTextView restartButton = (AppCompatTextView) findViewById(R.id.restartButton);

        final AppCompatTextView reDoWalkButton = (AppCompatTextView) findViewById(R.id.redoWalkButton);

        final AppCompatTextView saveButton = (AppCompatTextView) findViewById(R.id.saveButton);

        final TextView walkLogDisplay = (TextView) findViewById(R.id.walkLogDisplay);
        walkLogDisplay.setMovementMethod(new ScrollingMovementMethod());

        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/AlcoGaitDataGatherer/";

        File alcoGaitDirectory = new File(baseDir);
        alcoGaitDirectory.mkdirs();

        String fileName = "ID_" + testSubject.getSubjectID().trim() + ".csv";
        mFilename = baseDir + File.separator + fileName;


        gaitRecorder = new GaitRecorder(this, mFilename, testSubject, walkNumberDisplay, walkLogDisplay);
        gaitRecorder.registerListeners();

        beginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bacInput.getText().toString().trim().isEmpty()){
                    bacInput.setError("Please enter BAC data.");
                }else{
                    BAC = Double.valueOf(bacInput.getText().toString().trim());

                    gaitRecorder.startRecording(BAC);
                    // 3
                    beginButton.setVisibility(View.GONE);
                    bacInput.setEnabled(false);
                    countdown_title.setVisibility(View.VISIBLE);
                    walkNumberDisplay.setVisibility(View.VISIBLE);
                    restartButton.setEnabled(false);
                    reDoWalkButton.setEnabled(false);
                    saveButton.setEnabled(false);

                    // 4
                    final CountDownTimer count = new CountDownTimer(RECORD_TIME_IN_SECONDS * 1000, 1000) {

                        // 5
                        public void onTick(long millisUntilFinished) {
                            countdown.setText(String.valueOf(millisUntilFinished / 1000));

                            if((millisUntilFinished / 1000) == 30){
                                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                                toneGen1.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,1000);
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(1000);
                            }
                        }

                        // 6
                        public void onFinish() {
                            stopCurrentSession(countdown_title, beginButton, stopButton, bacInput, this);
                            bacInput.setEnabled(true);
                            createToolTip(bacInput, Tooltip.Gravity.RIGHT, "Updated BAC for next walk");
                            restartButton.setEnabled(true);
                            reDoWalkButton.setEnabled(true);
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
                            gaitRecorder.restartDataCollection(bacInput);
                            count.onTick(RECORD_TIME_IN_SECONDS * 1000);

                        }
                    });

                    reDoWalkButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            gaitRecorder.reDoWalk(bacInput);
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
                                            Window window = getWindow();
                                            gaitRecorder.writeToCSV(window);
                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            //No button clicked
                                            break;
                                    }
                                }
                            };
                            AlertDialog.Builder builder = new AlertDialog.Builder(DataGatheringActivity.this);
                            builder.setTitle("Save Walks");
                            builder.setMessage("Do you want to complete survey and save data to a CSV file?").setPositiveButton("Yes", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        gaitRecorder.registerListeners();
    }

    @Override
    protected void onPause(){
        super.onPause();
        gaitRecorder.unregisterListeners();
    }

    @Override
    protected void onStop(){
        super.onStop();
        gaitRecorder.unregisterListeners();
    }


    private void stopCurrentSession(TextView countdown_title, Button beginButton, Button stopButton,
                                    EditText bacInput, CountDownTimer countDownTimer){
        countdown_title.setVisibility(View.GONE);
        beginButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.GONE);
        countDownTimer.onTick(RECORD_TIME_IN_SECONDS * 1000);

        gaitRecorder.stopRecording(bacInput);
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
                        READ_WRITE_PERMISSION_CODE);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

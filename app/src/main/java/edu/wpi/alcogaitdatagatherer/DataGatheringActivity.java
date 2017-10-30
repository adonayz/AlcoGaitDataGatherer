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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.File;

import it.sephiroth.android.library.tooltip.Tooltip;

public class DataGatheringActivity extends AppCompatActivity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    // Android Layout Variables
    private TextView countdownTextField;
    private TextView countdown_title;
    private Button startButton;
    private Button stopButton;
    private TextView walkNumberDisplay;
    private EditText bacInput;
    private AppCompatTextView restartButton;
    private AppCompatTextView reDoWalkButton;
    private AppCompatTextView saveButton;
    private TextView walkLogDisplay;

    private String mFilename;
    private CountDownTimer countDownTimer;
    private GaitRecorder gaitRecorder;
    private boolean isRecording = false;
    private final int RECORD_TIME_IN_SECONDS= 60;
    private TestSubject testSubject;
    private Double BAC;
    private GoogleApiClient mGoogleApiClient;
    public static final String WEAR_HOME_ACTIVITY_PATH = "/start/WearHomeActivity";
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

        initViews();

        configureButtons();

        prepareStorageFile();

        gaitRecorder = new GaitRecorder(this, mFilename, testSubject, walkNumberDisplay, walkLogDisplay);
        gaitRecorder.registerListeners();

        connecClientForWearable();
    }

    private void initViews(){
        //TextView title = (TextView) findViewById(R.id.title);
        //TextView text = (TextView) findViewById(R.id.summary);

        countdownTextField = (TextView) findViewById(R.id.countdown);
        countdownTextField.setText(Integer.toString(RECORD_TIME_IN_SECONDS));
        countdown_title = (TextView) findViewById(R.id.countdown_title);
        startButton = (Button) findViewById(R.id.start_recording);
        stopButton = (Button) findViewById(R.id.stop_recording);
        walkNumberDisplay = (TextView) findViewById(R.id.walkNumberDisplay);
        bacInput = (EditText) findViewById(R.id.bacInput);
        restartButton = (AppCompatTextView) findViewById(R.id.restartButton);
        reDoWalkButton = (AppCompatTextView) findViewById(R.id.redoWalkButton);
        saveButton = (AppCompatTextView) findViewById(R.id.saveButton);
        walkLogDisplay = (TextView) findViewById(R.id.walkLogDisplay);
        walkLogDisplay.setMovementMethod(new ScrollingMovementMethod());
    }

    private void configureButtons(){
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bacInput.getText().toString().trim().isEmpty()){
                    bacInput.setError("Please enter BAC data.");
                }else{
                    startRecording();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDownTimer.onFinish();
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gaitRecorder.restartDataCollection(bacInput);
                countDownTimer.onTick(RECORD_TIME_IN_SECONDS * 1000);
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

    private void prepareStorageFile(){
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/AlcoGaitDataGatherer/";
        File alcoGaitDirectory = new File(baseDir);
        alcoGaitDirectory.mkdirs();
        String fileName = "ID_" + testSubject.getSubjectID().trim() + ".csv";
        mFilename = baseDir + File.separator + fileName;
    }

    private void setupTimer(){
        // 4
        countDownTimer = new CountDownTimer(RECORD_TIME_IN_SECONDS * 1000, 1000) {

            // 5
            public void onTick(long millisUntilFinished) {
                countdownTextField.setText(String.valueOf(millisUntilFinished / 1000));

                if((millisUntilFinished / 1000) == 30){
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,1000);
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(1000);
                }
            }

            // 6
            public void onFinish() {
                stopRecording();
            }
        };
    }

    private void startRecording(){
        if(!isRecording){
            isRecording = true;

            BAC = Double.valueOf(bacInput.getText().toString().trim());

            gaitRecorder.startRecording(BAC);

            startButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);
            bacInput.setEnabled(false);
            countdown_title.setVisibility(View.VISIBLE);
            walkNumberDisplay.setVisibility(View.VISIBLE);
            restartButton.setEnabled(false);
            reDoWalkButton.setEnabled(false);
            saveButton.setEnabled(false);

            setupTimer();

            countDownTimer.start();
        }
    }

    private void stopRecording(){
        if(isRecording){
            isRecording = false;

            countdown_title.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.GONE);

            countDownTimer.cancel();
            countDownTimer.onTick(RECORD_TIME_IN_SECONDS * 1000);

            gaitRecorder.stopRecording(bacInput);

            bacInput.setEnabled(true);
            createToolTip(bacInput, Tooltip.Gravity.RIGHT, "Updated BAC for next walk");
            restartButton.setEnabled(true);
            reDoWalkButton.setEnabled(true);
            saveButton.setEnabled(true);
        }
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

    private void connecClientForWearable(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    private void openWearableActivity(){
        //Open Wearable Activity
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for (Node node : getConnectedNodesResult.getNodes()) {
                    sendOpenInstruction(node.getId());
                }
            }
        });
    }

    private void sendOpenInstruction(String node) {
        Wearable.MessageApi.sendMessage(mGoogleApiClient , node , WEAR_HOME_ACTIVITY_PATH , new byte[0]).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                if (!sendMessageResult.getStatus().isSuccess()) {
                    Log.e("GoogleApi", "Failed to send message with status code: "
                            + sendMessageResult.getStatus().getStatusCode());
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("GoogleApi", "onConnected: " + bundle);
        openWearableActivity();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("GoogleApi", "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("GoogleApi", "onConnectionFailed: " + connectionResult);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }

    private void notifyWearableActivity(final String path, final String text){
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, text.getBytes() ).await();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }
}

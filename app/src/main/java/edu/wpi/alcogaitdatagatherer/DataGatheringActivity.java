package edu.wpi.alcogaitdatagatherer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
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
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.File;

import edu.wpi.alcogaitdatagatherercommon.CommonValues;
import it.sephiroth.android.library.tooltip.Tooltip;

import static android.view.View.LAYER_TYPE_HARDWARE;
import static android.view.View.LAYER_TYPE_NONE;

public class DataGatheringActivity extends AppCompatActivity implements MessageApi.MessageListener, DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, WalkReportFragment.OnListFragmentInteractionListener {

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
    private FrameLayout progressBarHolder;
    private RelativeLayout bottomBarLayout;

    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;

    private String mFilename;
    private CountDownTimer countDownTimer;
    private SensorRecorder sensorRecorder;
    private TestSubject testSubject;
    private GoogleApiClient mGoogleApiClient;
    private boolean isWearableConnected = false;
    private static final int READ_WRITE_PERMISSION_CODE = 1000;
    static final String TB_FOR_WALK_REPORT = "walk_report";

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

        sensorRecorder = new SensorRecorder(this, mFilename, testSubject, walkNumberDisplay, walkLogDisplay);

        connecClientForWearable();

        //startProgressBar();

        setupTimer();
    }

    private void initViews(){
        //TextView title = (TextView) findViewById(R.id.title);
        //TextView text = (TextView) findViewById(R.id.summary);

        countdownTextField = (TextView) findViewById(R.id.countdown);
        countdownTextField.setText(Integer.toString(CommonValues.RECORD_TIME_IN_SECONDS));
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
        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
        bottomBarLayout = (RelativeLayout) findViewById(R.id.bottomBar);
        disableBar(true);
    }

    private void configureButtons(){
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bacInput.getText().toString().trim().isEmpty()){
                    bacInput.setError("Please enter BAC data.");
                }else{
                    startRecording(bacInput.getText().toString().trim(), false);
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorRecorder.restartDataCollection(bacInput);
                countDownTimer.onTick(CommonValues.RECORD_TIME_IN_SECONDS * 1000);
            }
        });

        reDoWalkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorRecorder.reDoWalk(bacInput);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                WalkReportFragment walkReportFragment = new WalkReportFragment();
                                Bundle bundle= walkReportFragment.getArguments();
                                if(bundle == null){
                                    bundle = new Bundle();
                                }
                                bundle.putSerializable(TB_FOR_WALK_REPORT, sensorRecorder.getTestSubject());
                                walkReportFragment.setArguments(bundle);
                                setFragment(walkReportFragment);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(DataGatheringActivity.this);
                builder.setTitle("Save Walks");
                builder.setMessage("Do you want to complete survey and save data into internal storage?").setPositiveButton("Yes", dialogClickListener)
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
        countDownTimer = new CountDownTimer(CommonValues.RECORD_TIME_IN_SECONDS * 1000, 1000) {

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
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,1000);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(1000);
                stopRecording();
            }
        };
    }

    private void startRecording(String bacInputText, boolean isActionFromWearable){
        if(!sensorRecorder.isRecording()){
            Double BAC = Double.valueOf(bacInputText);
            bacInput.setText(String.valueOf(BAC));

            if(!isActionFromWearable){
                notifyWearableActivity(CommonValues.START_RECORDING_PATH, bacInputText);
            }

            if(isActionFromWearable){
                bacInput.setText(String.valueOf(BAC));
            }

            sensorRecorder.startRecording(BAC);

            startButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);
            bacInput.setEnabled(false);
            countdown_title.setVisibility(View.VISIBLE);
            walkNumberDisplay.setVisibility(View.VISIBLE);
            disableBar(true);

            countDownTimer.start();
        }
    }

    private void stopRecording(){
        if(sensorRecorder.isRecording()){
            notifyWearableActivity(CommonValues.WEAR_MESSAGE_PATH, CommonValues.STOP_RECORDING);

            countdown_title.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.GONE);

            sensorRecorder.stopRecording();

            bacInput.setText("");
            bacInput.setEnabled(true);
            createToolTip(bacInput, Tooltip.Gravity.RIGHT, "Update BAC for next walk");
            disableBar(false);

            countDownTimer.cancel();
            setupTimer();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorRecorder.registerListeners();
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorRecorder.unregisterListeners();
    }

    @Override
    protected void onStop(){
        sensorRecorder.unregisterListeners();
        notifyWearableActivity(CommonValues.WEAR_MESSAGE_PATH, CommonValues.WEARABLE_DISCONNECTED);
        notifyWearableActivity(CommonValues.WEAR_HOME_ACTIVITY_PATH, CommonValues.STOP_APP);

        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy(){
        sensorRecorder.unregisterListeners();
        notifyWearableActivity(CommonValues.WEAR_MESSAGE_PATH, CommonValues.WEARABLE_DISCONNECTED);
        notifyWearableActivity(CommonValues.WEAR_HOME_ACTIVITY_PATH, CommonValues.DESTROY_APP);
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
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

    public void requestSave(){
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
        }else{
            showSaveDialog();
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
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Log.d("GoogleApi", "onConnected: " + bundle);
        notifyWearableActivity(CommonValues.WEAR_HOME_ACTIVITY_PATH, CommonValues.OPEN_APP);
        notifyWearableActivity(CommonValues.WEAR_MESSAGE_PATH, CommonValues.WEARABLE_CONNECTED);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("GoogleApi", "onConnectionSuspended: " + i);
        /*Intent intent = new Intent(this, edu.wpi.alcogaitdatagatherer.SensorRecorder.class);
        stopService(intent);*/
        isWearableConnected = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("GoogleApi", "onConnectionFailed: " + connectionResult);
        isWearableConnected = false;
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                if(messageEvent.getPath().equalsIgnoreCase( CommonValues.WEAR_MESSAGE_PATH) ) {
                    switch (new String(messageEvent.getData())){
                        case CommonValues.STOP_RECORDING:
                            stopRecording();
                            break;
                        case CommonValues.CONFIRM_WALK:
                            break;
                        case CommonValues.REDO_PREVIOUS_WALK:
                            break;
                        case CommonValues.RESTART_SURVEY:
                            break;
                        case CommonValues.SAVE_SURVEY:
                            break;
                        case CommonValues.WEARABLE_DISCONNECTED:
                            isWearableConnected = false;
                            break;
                        case CommonValues.WEARABLE_CONNECTED:
                            if(!isWearableConnected){
                                isWearableConnected = true;
                                stopProgressBar();
                                showToast("Wearable Connected");
                                notifyWearableActivity(CommonValues.WEAR_MESSAGE_PATH, CommonValues.WEARABLE_CONNECTED);
                            }
                            break;
                        default:
                            break;
                    }
                }else if(messageEvent.getPath().equalsIgnoreCase(CommonValues.START_RECORDING_PATH)) {
                    startRecording(new String(messageEvent.getData()),true);
                }
            }
        });
    }

    private void startProgressBar(){
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        progressBarHolder.setAnimation(inAnimation);
        progressBarHolder.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void stopProgressBar(){
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        progressBarHolder.setAnimation(outAnimation);
        progressBarHolder.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if(sensorRecorder!= null && sensorRecorder.isRecording()){
            for (DataEvent dataEvent : dataEvents) {
                if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                    DataItem dataItem = dataEvent.getDataItem();
                    Uri uri = dataItem.getUri();
                    String path = uri.getPath();

                    if (path.startsWith(CommonValues.SENSOR_PATH)) {
                        sensorRecorder.addHeartRateData(
                               uri.getLastPathSegment(),
                                DataMapItem.fromDataItem(dataItem).getDataMap()
                        );
                    }
                }
            }
        }
    }


    private void showToast(final String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    public void disableBar(boolean disableBar) {
        if (disableBar) {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0.5f);
            Paint greyscalePaint = new Paint();
            greyscalePaint.setColorFilter(new ColorMatrixColorFilter(cm));
            bottomBarLayout.setLayerType(LAYER_TYPE_HARDWARE, greyscalePaint);
            for(int i = 0; i < bottomBarLayout.getChildCount(); i++){
                ((AppCompatTextView)bottomBarLayout.getChildAt(i)).setEnabled(false);
            }
        } else {
            bottomBarLayout.setLayerType(LAYER_TYPE_NONE, null);
            for(int i = 0; i < bottomBarLayout.getChildCount(); i++){
                ((AppCompatTextView)bottomBarLayout.getChildAt(i)).setEnabled(true);
            }
        }
    }

    protected void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);

// Commit the transaction
        transaction.commit();
    }

    private void showSaveDialog(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Window window = getWindow();
                        sensorRecorder.writeToCSV(window);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(DataGatheringActivity.this);
        builder.setTitle("Save Walks");
        builder.setMessage("Do you want to complete survey and save data into internal storage?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_WRITE_PERMISSION_CODE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showSaveDialog();
                } else {
                    // permission denied by user. disable
                }
                return;
            }
        }
    }

    @Override
    public void onListFragmentInteraction(Walk walk) {

    }
}

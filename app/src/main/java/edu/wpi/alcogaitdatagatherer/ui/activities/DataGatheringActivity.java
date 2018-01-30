package edu.wpi.alcogaitdatagatherer.ui.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import edu.wpi.alcogaitdatagatherer.R;
import edu.wpi.alcogaitdatagatherer.models.SensorRecorder;
import edu.wpi.alcogaitdatagatherer.models.TestSubject;
import edu.wpi.alcogaitdatagatherer.ui.fragments.WalkReportFragment;
import edu.wpi.alcogaitdatagatherercommon.CommonCode;
import edu.wpi.alcogaitdatagatherercommon.WalkType;
import it.sephiroth.android.library.tooltip.Tooltip;

import static android.view.View.LAYER_TYPE_HARDWARE;
import static android.view.View.LAYER_TYPE_NONE;

public class DataGatheringActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener, WalkReportFragment.ReportFragmentListener {

    // Android Layout Variables
    private TextView countdownTextField;
    private TextView countdown_title;
    private Button startButton;
    private Button stopButton;
    private TextView walkNumberDisplay;
    private EditText bacInput;
    private AppCompatTextView restartButton;
    private AppCompatTextView reDoWalkButton;
    private AppCompatTextView finishButton;
    private TextView walkLogDisplay;
    private FrameLayout progressBarHolder;
    private RelativeLayout bottomBarLayout;
    private TextView wearConnectProgressUpdateTextView;

    private String mFolderName;
    private CountDownTimer countDownTimer;
    private SensorRecorder sensorRecorder;
    private TestSubject testSubject;
    private boolean isReceivingFromWatch = false;
    private boolean allowBACInput = true;
    private static final int READ_WRITE_PERMISSION_CODE = 1000;
    public static final String TB_FOR_WALK_REPORT = "walk_report";
    private int samplesReceivedFromWatch = 0;
    private int totalSampleSizeInWearable = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_gathering);

        Toolbar toolbar = findViewById(R.id.survey_toolbar);
        toolbar.setTitle("Record Gait Data");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent prevIntent = getIntent();
        testSubject= (TestSubject) prevIntent.getSerializableExtra("test_subject");

        initViews();

        configureButtons();

        prepareStoragePath();

        sensorRecorder = new SensorRecorder(this, mFolderName, testSubject, walkNumberDisplay, walkLogDisplay, startButton);

        setupTimer();
    }

    private void initViews(){
        //TextView title = (TextView) findViewById(R.id.title);
        //TextView text = (TextView) findViewById(R.id.summary);

        countdownTextField = findViewById(R.id.countdown);
        countdownTextField.setText(Integer.toString(CommonCode.RECORD_TIME_IN_SECONDS));
        countdown_title = findViewById(R.id.countdown_title);
        startButton = findViewById(R.id.start_recording);
        stopButton = findViewById(R.id.stop_recording);
        walkNumberDisplay = findViewById(R.id.walkNumberDisplay);
        bacInput = findViewById(R.id.bacInput);
        restartButton = findViewById(R.id.restartButton);
        reDoWalkButton = findViewById(R.id.redoWalkButton);
        finishButton = findViewById(R.id.finishButton);
        finishButton = findViewById(R.id.finishButton);
        walkLogDisplay = findViewById(R.id.walkLogDisplay);
        walkLogDisplay.setMovementMethod(new ScrollingMovementMethod());
        progressBarHolder = findViewById(R.id.progressBarHolder);
        bottomBarLayout = findViewById(R.id.bottomBar);
        wearConnectProgressUpdateTextView = findViewById(R.id.wearConnectProgressUpdateTextView);
        disableBar(true);

        bacInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String bacString = editable.toString().trim();
                if (!bacString.isEmpty()) {
                    if (bacString.startsWith(".")) {
                        bacString = "0" + bacString;
                    }
                    if (Double.parseDouble(bacString) > 5.0) {
                        bacInput.setError("Invalid. 5.0 Maximum");
                    } else {
                        bacInput.setError(null);
                    }
                }
            }
        });
    }

    private void configureButtons(){
        startButton.setOnClickListener(v -> {
            allowBACInput = sensorRecorder.getCurrentWalkType() == WalkType.NORMAL;
            if ((bacInput.getText().toString().trim().isEmpty())) {
                bacInput.setError("Please enter BAC data.");
            }

            if (bacInput.getError() == null) {
                startRecording(bacInput.getText().toString().trim());
            }
        });

        stopButton.setOnClickListener(view -> stopRecording());

        restartButton.setOnClickListener(view -> {
            sensorRecorder.restartCurrentWalkNumber(bacInput, this);
            countDownTimer.onTick(CommonCode.RECORD_TIME_IN_SECONDS * 1000);
            if (isWearablePreferenceEnabled()) {
                notifyWearableActivity(CommonCode.WEAR_MESSAGE_PATH, CommonCode.RESTART);
            }
        });

        reDoWalkButton.setOnClickListener(view -> {
            if (sensorRecorder.getTestSubject().getCurrentWalkHolder().hasWalk(WalkType.NORMAL)) {
                reDoWalkButton.setError(null);
                sensorRecorder.reDoWalk(bacInput, this);
            } else {
                reDoWalkButton.setError("");
                createToolTip(reDoWalkButton, Tooltip.Gravity.TOP, "You can only re-do walks from the same walk number. " +
                        "You have not recorded any data for the current walk number (#"
                        + sensorRecorder.getTestSubject().getCurrentWalkHolder().getWalkNumber() + ").");
            }
        });

        finishButton.setOnClickListener(view -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        WalkReportFragment walkReportFragment = new WalkReportFragment();
                        Bundle bundle = walkReportFragment.getArguments();
                        if (bundle == null) {
                            bundle = new Bundle();
                        }
                        bundle.putSerializable(TB_FOR_WALK_REPORT, sensorRecorder.getTestSubject());
                        walkReportFragment.setArguments(bundle);
                        setFragment(walkReportFragment);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        returnToHomeScreen();
                        break;
                }
            };
            final AlertDialog.Builder builder = new AlertDialog.Builder(DataGatheringActivity.this);
            builder.setTitle("Report Walks");
            builder.setMessage("Would you like to submit a report about any of the walks?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener);

            DialogInterface.OnClickListener dialogClickListener2 = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        builder.show();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };
            AlertDialog.Builder builder2 = new AlertDialog.Builder(DataGatheringActivity.this);
            builder2.setTitle("Finish Survey");
            builder2.setMessage("Are you sure you want to finish survey? Unfinished walk number data will be lost (Walk Number " +
                    sensorRecorder.getTestSubject().getCurrentWalkHolder().getWalkNumber() + ").").setPositiveButton("Yes", dialogClickListener2)
                    .setNegativeButton("No", dialogClickListener2).show();
        });
    }

    private void prepareStoragePath() {
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/AlcoGaitDataGatherer/";
        String folderName = "ID_" + testSubject.getSubjectID().trim();
        mFolderName = baseDir + folderName;
        File surveyStorageDirectory = new File(mFolderName);
        surveyStorageDirectory.mkdirs();
    }

    private void setupTimer(){
        countDownTimer = new CountDownTimer(CommonCode.RECORD_TIME_IN_SECONDS * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                countdownTextField.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,1000);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    v.vibrate(1000);
                } else {
                    showToast("UNABLE TO VIBRATE");
                }
                stopRecording();
            }
        };
    }

    private void startRecording(String bacInputText) {
        if(!sensorRecorder.isRecording()){
            if (sensorRecorder.getTestSubject().getCurrentWalkHolder().getNextWalkType() == null) {
                sensorRecorder.prepareWalkStorage();
                requestSave();
                if (isWearablePreferenceEnabled()) {
                    startProgressBar();
                    sensorRecorder.setActivity(this);
                    updateProgressBarMessage("Waiting For Watch Data");
                    notifyWearableActivity(CommonCode.WEAR_MESSAGE_PATH, CommonCode.SAVE_WALKS);
                }
            } else {
                if (bacInputText.startsWith(".")) {
                    bacInputText = "0" + bacInputText;
                }
                Double BAC = Double.valueOf(bacInputText);
                bacInput.setText(String.valueOf(BAC));
                samplesReceivedFromWatch = 0;
                totalSampleSizeInWearable = 0;

                sensorRecorder.startRecording(BAC);

                if (isWearablePreferenceEnabled()) {
                    notifyWearableActivity(CommonCode.START_RECORDING_PATH, sensorRecorder.getCurrentWalkType().toNoSpaceString());
                }

                startButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.VISIBLE);
                bacInput.setEnabled(false);
                countdown_title.setVisibility(View.VISIBLE);
                walkNumberDisplay.setVisibility(View.VISIBLE);
                disableBar(true);

                countDownTimer.start();
            }
        }
    }

    private void stopRecording(){
        if(sensorRecorder.isRecording()){
            if (isWearablePreferenceEnabled()) {
                if (isReceivingFromWatch) {
                    return;
                }
                notifyWearableActivity(CommonCode.WEAR_MESSAGE_PATH, CommonCode.STOP_RECORDING);
                startProgressBar();
                //isReceivingFromWatch = true;
            }

            countdown_title.setVisibility(View.GONE);
            countDownTimer.cancel();
            setupTimer();
            stopButton.setVisibility(View.GONE);

            // TODO WAIT FOR WATCH AFTER EACH WALK
            /*if (!isWearablePreferenceEnabled()) {
                resetRecordViews(sensorRecorder.stopRecording());
            }*/
            resetRecordViews(sensorRecorder.stopRecording());
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (sensorRecorder != null) {
            if (sensorRecorder.isRecording()) {
                sensorRecorder.registerListeners();
            }
        }
        if (isWearablePreferenceEnabled()) {
            new CheckWearableReachability().execute();
            Wearable.getCapabilityClient(this).addListener(
                    this,
                    CommonCode.WEAR_DISCOVERY_NAME);
            Wearable.getMessageClient(this).addListener(this);
            Wearable.getChannelClient(this).registerChannelCallback(sensorRecorder);
            notifyWearableActivity(CommonCode.WEAR_HOME_ACTIVITY_PATH, CommonCode.OPEN_APP);
        }
    }

    @Override
    protected void onPause(){
        if (sensorRecorder != null) {
            if (sensorRecorder.isRecording()) {
                sensorRecorder.unregisterListeners();
            }
        }
        if (isWearablePreferenceEnabled()) {
            notifyWearableActivity(CommonCode.WEAR_MESSAGE_PATH, CommonCode.WEARABLE_DISCONNECTED);
            Wearable.getCapabilityClient(this).removeListener(this, CommonCode.WEAR_DISCOVERY_NAME);
            Wearable.getMessageClient(this).removeListener(this);
            Wearable.getChannelClient(this).unregisterChannelCallback(sensorRecorder);
        }
        super.onPause();
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

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        READ_WRITE_PERMISSION_CODE);
            }
        }else{
            saveCurrentWalkNumber();
        }

    }

    public void notifyWearableActivity(final String path, final String text) {
        if (isWearablePreferenceEnabled()) {
            new Thread(() -> {
                for (String nodeID : getNodes()) {
                    Wearable.getMessageClient(DataGatheringActivity.this).sendMessage(
                            nodeID, path, text.getBytes());
                }
            }).start();
        }
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();
        List<Node> nodes;
        try {
            nodes = Tasks.await(Wearable.getNodeClient(this).getConnectedNodes());
            for (Node node : nodes) {
                results.add(node.getId());
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        if (capabilityInfo.getNodes().size() > 0) {
            showToast("WATCH IS CONNECTED");
        } else {
            showToast("WATCH IS DISCONNECTED");
        }
    }

    @Override
    public void onMessageReceived(@NonNull final MessageEvent messageEvent) {
        runOnUiThread(() -> {
            if (messageEvent.getPath().equalsIgnoreCase(CommonCode.WEAR_MESSAGE_PATH)) {
                switch (new String(messageEvent.getData())) {
                    case CommonCode.REDO_PREVIOUS_WALK_ACK:
                        break;
                    case CommonCode.RESTART_ACK:
                        break;
                    case CommonCode.SAVE_WALKS_ACK:
                        break;
                    case CommonCode.WEARABLE_DISCONNECTED:
                        showToast("WEARABLE DISCONNECTED");
                        break;
                    case CommonCode.CHECK_IF_APP_OPEN:
                        notifyWearableActivity(CommonCode.WEAR_MESSAGE_PATH, CommonCode.APP_OPEN_ACK);
                        stopProgressBar();
                        showToast("CONNECTION REQUEST RECEIVED");
                        break;
                    case CommonCode.APP_OPEN_ACK:
                        stopProgressBar();
                        showToast("CONNECTION CONFIRMED");
                        break;
                    default:
                        break;
                }
            } else if (messageEvent.getPath().equalsIgnoreCase(CommonCode.STOP_RECORDING_PATH)) {
                totalSampleSizeInWearable = Integer.valueOf(new String(messageEvent.getData()));
                stopRecording();
            }
        });
    }

    /*@Override
    public void onDataChanged(@NonNull DataEventBuffer dataEvents) {
        if(sensorRecorder!= null && sensorRecorder.isRecording()){
            for (DataEvent dataEvent : dataEvents) {
                if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                    DataItem dataItem = dataEvent.getDataItem();
                    Uri uri = dataItem.getUri();
                    String path = uri.getPath();

                    if (path.startsWith(CommonCode.SENSOR_PATH)) {
                        if (DataMapItem.fromDataItem(dataItem).getDataMap().getString(CommonCode.SENSOR_NAME).equals(CommonCode.TRANSFER_FINISHED_STRING) &&
                                DataMapItem.fromDataItem(dataItem).getDataMap().getLong(CommonCode.TIMESTAMP) == CommonCode.TRANSFER_FINISHED_LONG) {
                            stopWaitingForWatch();
                            return;
                        }
                        sensorRecorder.addWearableSensorData(
                                Integer.parseInt(uri.getLastPathSegment()),
                                DataMapItem.fromDataItem(dataItem).getDataMap()
                        );
                        samplesReceivedFromWatch++;
                        if (totalSampleSizeInWearable != 0) {
                            String status = String.valueOf(samplesReceivedFromWatch) + "/" + String.valueOf(totalSampleSizeInWearable);
                            wearConnectProgressUpdateTextView.setText(String.format("%s\n%s", getString(R.string.collecting_from_watch), status));
                        } else {
                            wearConnectProgressUpdateTextView.setText(getString(R.string.collecting_from_watch));
                        }
                    }
                }
            }
        }
    }*/

    public void startProgressBar() {
        AlphaAnimation inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        progressBarHolder.setAnimation(inAnimation);
        progressBarHolder.setVisibility(View.VISIBLE);
    }

    public void stopProgressBar() {
        AlphaAnimation outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        progressBarHolder.setAnimation(outAnimation);
        progressBarHolder.setVisibility(View.GONE);
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
                (bottomBarLayout.getChildAt(i)).setEnabled(false);
            }
        } else {
            bottomBarLayout.setLayerType(LAYER_TYPE_NONE, null);
            for(int i = 0; i < bottomBarLayout.getChildCount(); i++){
                (bottomBarLayout.getChildAt(i)).setEnabled(true);
            }
        }
    }

    protected void setFragment(WalkReportFragment fragment) {
        FragmentManager manager = getFragmentManager();
        fragment.show(manager, "FRAGMENT");
    }

    private void saveCurrentWalkNumber() {
        sensorRecorder.saveCurrentWalkNumberToCSV(bacInput);
        //createToolTip(bacInput, Tooltip.Gravity.RIGHT, "Update BAC for next walk");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_WRITE_PERMISSION_CODE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveCurrentWalkNumber();
                }
            }
        }
    }

    private void resetRecordViews(boolean allowInput) {
        if (isWearablePreferenceEnabled()) {
            stopProgressBar();
        }
        allowBACInput = allowInput;
        startButton.setVisibility(View.VISIBLE);
        // temporarily disallow changing BAC in the same walk number
        /*if (allowInput) {
            bacInput.setEnabled(true);
            createToolTip(bacInput, Tooltip.Gravity.RIGHT, "Update BAC for next walk");
        }*/
        disableBar(false);
    }

    public void stopWaitingForWatch() {
        boolean allowInput = true;
        if (sensorRecorder.isRecording()) {
            allowInput = sensorRecorder.stopRecording();
        }
        resetRecordViews(allowInput);
        isReceivingFromWatch = false;
    }

    @Override
    public void submitReport(LinkedList<Boolean> checkBoxStates, String reportMessage) {
        TestSubject testSubject = sensorRecorder.getTestSubject();

        testSubject.setBooleanWalksList(checkBoxStates);
        testSubject.setReportMessage(reportMessage);

        sensorRecorder.setTestSubject(testSubject);

        sensorRecorder.saveWalkReport();

        returnToHomeScreen();
    }

    void returnToHomeScreen() {
        finish();
        Intent intent = new Intent(DataGatheringActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    DataGatheringActivity.super.onBackPressed();
                    DataGatheringActivity.this.finish();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(DataGatheringActivity.this);
        builder.setTitle("Return To Subject Information Form?");
        builder.setMessage("Are you sure you want to return to the form? Data for the latest walk number will be lost (#"
                + sensorRecorder.getTestSubject().getCurrentWalkHolder().getWalkNumber() + ")").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public boolean isWearablePreferenceEnabled() {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return SP.getBoolean(getString(R.string.wear_collection_preference), false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isWearablePreferenceEnabled()) {
            getMenuInflater().inflate(R.menu.refresh_icon, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.refresh_watch_icon:
                if (isWearablePreferenceEnabled()) {
                    notifyWearableActivity(CommonCode.WEAR_MESSAGE_PATH, CommonCode.REFRESH_CONNECTION);
                    new CheckWearableReachability().execute();
                    //stopWaitingForWatch();
                    return true;
                }
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class CheckWearableReachability extends AsyncTask<Void, Void, CapabilityInfo> {
        @Override
        protected void onPreExecute() {
            startProgressBar();
            wearConnectProgressUpdateTextView.setText(R.string.searching);
        }

        @Override
        protected CapabilityInfo doInBackground(Void... voids) {
            CapabilityInfo capabilityInfo = null;
            try {
                capabilityInfo = Tasks.await(
                        Wearable.getCapabilityClient(DataGatheringActivity.this).getCapability(
                                CommonCode.WEAR_DISCOVERY_NAME, CapabilityClient.FILTER_REACHABLE));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return capabilityInfo;
        }

        @Override
        protected void onPostExecute(CapabilityInfo result) {
            if (result != null) {
                if (result.getNodes().size() > 0) {
                    showToast("WEARABLE IS REACHABLE");
                    wearConnectProgressUpdateTextView.setText("Awaiting Watch Response");
                    notifyWearableActivity(CommonCode.WEAR_MESSAGE_PATH, CommonCode.CHECK_IF_APP_OPEN);
                    return;
                }
            }
            watchReachabilityErrorDialog();
        }
    }

    public void watchReachabilityErrorDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(DataGatheringActivity.this);
        alert.setTitle("Watch Is Not Reachable");
        alert.setMessage(R.string.watch_reachability_error_dialog);
        alert.setPositiveButton("OK", null);
        alert.show();
    }

    public void updateProgressBarMessage(String message) {
        wearConnectProgressUpdateTextView.setText(message);
    }
}
